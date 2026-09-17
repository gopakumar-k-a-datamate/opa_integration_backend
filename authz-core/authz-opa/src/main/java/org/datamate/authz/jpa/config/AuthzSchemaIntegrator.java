package org.datamate.authz.jpa.config;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.Table;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.Collection;
import java.util.Set;

public class AuthzSchemaIntegrator implements Integrator {

    private final String targetSchema;

    private static final Set<String> KNOWN_AUTHZ_TABLES = Set.of(
            "authz_resource",
            "authz_permission",
            "authz_condition_field",
            "authz_policy",
            "authz_policy_bundle_cache",
            "authz_subject",
            "authz_resource_audit",
            "authz_permission_audit",
            "authz_policy_audit"
    );

    public AuthzSchemaIntegrator(String targetSchema) {
        this.targetSchema = targetSchema;
    }

    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        if (targetSchema == null || targetSchema.isBlank() || "public".equalsIgnoreCase(targetSchema)) {
            return;
        }

        for (org.hibernate.boot.model.relational.Namespace namespace : metadata.getDatabase().getNamespaces()) {
            for (Table table : namespace.getTables()) {
                String tableName = table.getName();
                if (tableName != null && KNOWN_AUTHZ_TABLES.contains(tableName.toLowerCase())) {
                    table.setSchema(targetSchema);
                }
            }
        }
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        // No-op
    }
}
