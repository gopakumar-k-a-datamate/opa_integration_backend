package org.datamate.authz.adapter.in.startup;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.shared.annotation.PolicyField;
import org.datamate.authz.shared.annotation.PolicyResource;
import org.datamate.authz.application.port.out.policy.ConditionFieldPersistencePort;
import org.datamate.authz.application.port.out.policy.PermissionPersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyPersistencePort;
import org.datamate.authz.application.port.out.policy.ResourcePersistencePort;
import org.datamate.authz.application.port.out.policy.PolicyCompilerPort;
import org.datamate.authz.domain.model.policy.entity.ConditionField;
import org.datamate.authz.domain.model.policy.entity.Permission;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.domain.model.policy.entity.Resource;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Inbound adapter that listens to Spring startup events to auto-register
 * all {@link PolicyResource}-annotated classes into the local {@code authz_*} tables
 * and perform diff-based field sync.
 */
@RequiredArgsConstructor
@Component
public class StartupScanner implements ApplicationListener<ContextRefreshedEvent> {

    private static final String BASE_PACKAGE = "";  // scan entire classpath

    private final ResourcePersistencePort resourcePort;
    private final PermissionPersistencePort permissionPort;
    private final ConditionFieldPersistencePort conditionFieldPort;
    private final PolicyPersistencePort policyPort;
    private final PolicyCompilerPort compilerPort;

    private volatile boolean alreadyRan = false;
@Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadyRan) return;
        alreadyRan = true;

        Set<BeanDefinition> candidates = scanForPolicyResources();

        for (BeanDefinition bd : candidates) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                PolicyResource annotation = clazz.getAnnotation(PolicyResource.class);
                if (annotation == null) continue;

                processAnnotation(clazz, annotation);
            } catch (ClassNotFoundException e) {
                // Skip unloadable classes
            }
        }

        // Compile the bundle after all registrations via application port
        compilerPort.recompile();
    }

    private Set<BeanDefinition> scanForPolicyResources() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(PolicyResource.class));
        return scanner.findCandidateComponents(BASE_PACKAGE);
    }

    private void processAnnotation(Class<?> clazz, PolicyResource annotation) {
        String namespace = annotation.namespace();
        String resourceName = annotation.name();
        String action = annotation.action();
        String description = annotation.description();

        Resource resource = resourcePort.upsert(
                UUID.randomUUID(), namespace, resourceName, description);

        String code = namespace + ":" + resourceName + ":" + action;
        Permission permission = permissionPort.upsert(
                UUID.randomUUID(), resource.getId(), action, code, description);

        Set<String> incomingFieldNames = collectIncomingFields(clazz);
        upsertFields(clazz, permission.getId());

        diffSyncRemovedFields(permission.getId(), incomingFieldNames);
    }

    private Set<String> collectIncomingFields(Class<?> clazz) {
        Set<String> fields = new HashSet<>();
        if (clazz.isRecord()) {
            for (RecordComponent rc : clazz.getRecordComponents()) {
                if (rc.isAnnotationPresent(PolicyField.class)) fields.add(rc.getName());
            }
        } else {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(PolicyField.class)) fields.add(f.getName());
            }
        }
        return fields;
    }

    private void upsertFields(Class<?> clazz, UUID permissionId) {
        if (clazz.isRecord()) {
            for (RecordComponent rc : clazz.getRecordComponents()) {
                PolicyField pf = rc.getAnnotation(PolicyField.class);
                if (pf != null) doUpsertField(permissionId, rc.getName(), pf);
            }
        } else {
            for (Field f : clazz.getDeclaredFields()) {
                PolicyField pf = f.getAnnotation(PolicyField.class);
                if (pf != null) doUpsertField(permissionId, f.getName(), pf);
            }
        }
    }

    private void doUpsertField(UUID permissionId, String fieldName, PolicyField pf) {
        FieldType fieldType = pf.type();
        String displayName = pf.displayName();
        List<String> allowedValues = pf.allowedValues().length > 0
                ? Arrays.asList(pf.allowedValues()) : null;
        String optionsEndpoint = pf.optionsEndpoint().isBlank() ? null : pf.optionsEndpoint();

        conditionFieldPort.upsert(UUID.randomUUID(), permissionId, fieldName,
                fieldType, displayName, allowedValues, optionsEndpoint);
    }

    private void diffSyncRemovedFields(UUID permissionId, Set<String> incomingFieldNames) {
        List<ConditionField> allDbFields = conditionFieldPort.findAllByPermissionId(permissionId);

        for (ConditionField dbField : allDbFields) {
            if (incomingFieldNames.contains(dbField.getFieldName())) continue;

            List<Policy> affectedPolicies = policyPort.findEnabledReferencingField(
                    permissionId, dbField.getFieldName());

            if (!affectedPolicies.isEmpty()) {
                conditionFieldPort.markDeprecated(dbField.getId());
                for (Policy policy : affectedPolicies) {
                    policyPort.autoDisable(policy.getId(),
                            "Field '" + dbField.getFieldName() + "' was removed from code");
                }
            } else {
                conditionFieldPort.softDelete(dbField.getId());
            }
        }
    }
}




