package org.datamate.authz.shared.config;

import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot auto-configuration entry point for the {@code authz-core} library.
 *
 * <p>When this JAR is on the classpath of any Spring Boot application,
 * this configuration class automatically:</p>
 * <ul>
 *   <li>Registers all {@code authz-core} Spring beans (controllers, services, adapters, scanner)</li>
 *   <li>Enables JPA repositories under {@code org.datamate.authz}</li>
 *   <li>Scans JPA entities under {@code org.datamate.authz.adapter.out.persistence.entity}</li>
 * </ul>
 * </ul>
 */
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.flywaydb.core.Flyway;
import javax.sql.DataSource;

@AutoConfiguration(before = FlywayAutoConfiguration.class)
@org.springframework.context.annotation.EnableAspectJAutoProxy
@ComponentScan(basePackages = {
        "org.datamate.authz.adapter",
        "org.datamate.authz.application",
        "org.datamate.authz.domain.service.policy"
})
@EnableJpaRepositories(basePackages = "org.datamate.authz.adapter.out.persistence.policy.repository")
@EntityScan(basePackages = "org.datamate.authz.adapter.out.persistence.policy.entity")
@EnableTransactionManagement
public class AuthzCoreAutoConfiguration {

    @Bean
    public BeanPostProcessor authzFlywayMigrator(ObjectProvider<DataSource> dataSourceProvider) {
        return new BeanPostProcessor() {
            private boolean migrated = false;

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                if (!migrated && ("flyway".equals(beanName) || "flywayInitializer".equals(beanName))) {
                    DataSource dataSource = dataSourceProvider.getIfAvailable();
                    if (dataSource != null) {
                        Flyway.configure()
                                .dataSource(dataSource)
                                .locations("classpath:db/authz-migration")
                                .table("authz_flyway_schema_history")
                                .load()
                                .migrate();
                        migrated = true;
                    }
                }
                return bean;
            }
        };
    }
}


