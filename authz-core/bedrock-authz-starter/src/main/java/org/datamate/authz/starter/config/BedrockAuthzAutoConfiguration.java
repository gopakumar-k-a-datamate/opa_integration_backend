package org.datamate.authz.starter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.flywaydb.core.Flyway;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Single entry point for Bedrock Authz auto-configuration.
 */
@AutoConfiguration(before = FlywayAutoConfiguration.class)
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {
        "org.datamate.authz.service",
        "org.datamate.authz.compiler",
        "org.datamate.authz.dto",
        "org.datamate.authz.adapter.out.opa",
        "org.datamate.authz.enforcement"
})
public class BedrockAuthzAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.datamate.authz.jpa.entity.PolicyJpaEntity")
    @ConditionalOnProperty(prefix = "authz.jpa", name = "enabled", matchIfMissing = true)
    @EnableJpaRepositories(basePackages = "org.datamate.authz.jpa.repository")
    @EntityScan(basePackages = "org.datamate.authz.jpa.entity")
    @ComponentScan(basePackages = "org.datamate.authz.jpa")
    @EnableTransactionManagement
    public static class JpaConfiguration {

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnClass(Flyway.class)
        public static class AuthzFlywayConfiguration {
            
            private final DataSource dataSource;

            public AuthzFlywayConfiguration(DataSource dataSource) {
                this.dataSource = dataSource;
            }
            
            @PostConstruct
            public void migrateAuthz() {
                Flyway.configure()
                        .dataSource(dataSource)
                        .locations("classpath:db/authz-migration")
                        .table("authz_flyway_schema_history")
                        .baselineOnMigrate(true)
                        .load()
                        .migrate();
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.datamate.authz.rest.controller.PolicyController")
    @ConditionalOnProperty(prefix = "authz.rest", name = "enabled", matchIfMissing = true)
    @ComponentScan(basePackages = "org.datamate.authz.rest")
    public static class RestConfiguration {
    }
}
