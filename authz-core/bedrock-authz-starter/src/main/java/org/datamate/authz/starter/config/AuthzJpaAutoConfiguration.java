package org.datamate.authz.starter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.flywaydb.core.Flyway;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Spring Boot auto-configuration for the JPA integration of the {@code authz-core} library.
 *
 * <p>Can be disabled by setting {@code authz.jpa.enabled=false}.</p>
 */
@AutoConfiguration(after = AuthzCoreAutoConfiguration.class, before = FlywayAutoConfiguration.class)
@ConditionalOnClass(name = "org.datamate.authz.jpa.entity.PolicyJpaEntity")
@ConditionalOnProperty(prefix = "authz.jpa", name = "enabled", matchIfMissing = true)
@EnableJpaRepositories(basePackages = "org.datamate.authz.jpa.repository")
@EntityScan(basePackages = "org.datamate.authz.jpa.entity")
@ComponentScan(basePackages = "org.datamate.authz.jpa")
@EnableTransactionManagement
public class AuthzJpaAutoConfiguration {

    @AutoConfiguration
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
