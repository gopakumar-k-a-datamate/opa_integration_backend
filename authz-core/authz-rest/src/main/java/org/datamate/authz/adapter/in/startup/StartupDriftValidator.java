package org.datamate.authz.adapter.in.startup;

import lombok.RequiredArgsConstructor;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.authz.application.port.out.policy.ConditionFieldPersistencePort;
import org.datamate.authz.application.port.out.policy.PermissionPersistencePort;
import org.datamate.authz.domain.model.policy.entity.ConditionField;
import org.datamate.authz.domain.model.policy.entity.Permission;
import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.datamate.authz.shared.exception.SchemaDriftException;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates that Java annotations match the Database schema on startup.
 * Halts boot if there is any drift (missing SQL migrations).
 */
@RequiredArgsConstructor
@Component
public class StartupDriftValidator implements ApplicationListener<ContextRefreshedEvent> {

    @EnableLogger
    private Logger log;

    private final PermissionPersistencePort permissionPort;
    private final ConditionFieldPersistencePort conditionFieldPort;

    private volatile boolean alreadyRan = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadyRan) return;
        alreadyRan = true;

        log.info("Starting Java to DB Drift Validation...");

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(PolicyResource.class));
        
        Set<BeanDefinition> candidates = scanner.findCandidateComponents("org.datamate");
        Set<String> javaPermissionCodes = new HashSet<>();

        for (BeanDefinition bd : candidates) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                PolicyResource annotation = clazz.getAnnotation(PolicyResource.class);
                if (annotation == null) continue;

                String permissionCode = annotation.namespace() + ":" + annotation.name() + ":" + annotation.action();
                javaPermissionCodes.add(permissionCode);

                Set<String> javaFields = new HashSet<>();
                if (clazz.isRecord()) {
                    for (RecordComponent rc : clazz.getRecordComponents()) {
                        PolicyField pf = rc.getAnnotation(PolicyField.class);
                        if (pf != null) javaFields.add(rc.getName());
                    }
                } else {
                    for (Field f : clazz.getDeclaredFields()) {
                        PolicyField pf = f.getAnnotation(PolicyField.class);
                        if (pf != null) javaFields.add(f.getName());
                    }
                }

                Optional<Permission> optPermission = permissionPort.findByCode(permissionCode);
                if (optPermission.isEmpty()) {
                    throw new SchemaDriftException("Drift Detected: Missing SQL migration. Permission '" + permissionCode + "' exists in Java but not in Database.");
                }

                Permission permission = optPermission.get();
                List<ConditionField> dbFields = conditionFieldPort.findAllByPermissionId(permission.getId());
                Set<String> activeDbFields = dbFields.stream()
                        .filter(f -> !f.isDeprecated())
                        .map(ConditionField::getFieldName)
                        .collect(Collectors.toSet());

                // Check Java -> DB drift
                for (String javaField : javaFields) {
                    if (!activeDbFields.contains(javaField)) {
                        throw new SchemaDriftException("Drift Detected: Missing SQL migration. Field '" + javaField + "' in '" + permissionCode + "' exists in Java but not in Database.");
                    }
                }

                // Check DB -> Java drift
                for (String dbField : activeDbFields) {
                    if (!javaFields.contains(dbField)) {
                        throw new SchemaDriftException("Drift Detected: Missing SQL migration. Field '" + dbField + "' in '" + permissionCode + "' exists in Database but was removed from Java. You must write a SQL script to deprecate this field.");
                    }
                }

            } catch (ClassNotFoundException e) {
                log.warn("Class not found during scan: " + bd.getBeanClassName());
            }
        }
        
        // Check DB -> Java drift for permissions
        List<Permission> allDbPermissions = permissionPort.findAllActive();
        for (Permission dbPermission : allDbPermissions) {
            if (!javaPermissionCodes.contains(dbPermission.getCode())) {
                throw new SchemaDriftException("Drift Detected: Missing SQL migration. Permission '" + dbPermission.getCode() + "' exists in Database but the corresponding @PolicyResource class was removed from Java. You must write a SQL script to soft-delete this permission.");
            }
        }

        log.info("Drift validation passed successfully.");
    }
}
