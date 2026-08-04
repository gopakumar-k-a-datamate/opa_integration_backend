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
import org.flywaydb.core.api.Location;

/**
 * Spring Boot auto-configuration for the JPA integration of the {@code authz-core} library.
 *
 * <p>Can be disabled by setting {@code authz.jpa.enabled=false}.</p>
 */
@AutoConfiguration(after = AuthzCoreAutoConfiguration.class, before = FlywayAutoConfiguration.class)
@ConditionalOnProperty(prefix = "authz.jpa", name = "enabled", matchIfMissing = true)
@EnableJpaRepositories(basePackages = "org.datamate.authz.jpa.repository")
@EntityScan(basePackages = "org.datamate.authz.jpa.entity")
@ComponentScan(basePackages = "org.datamate.authz.jpa")
@EnableTransactionManagement
public class AuthzJpaAutoConfiguration {

    @Bean
    @ConditionalOnClass(Flyway.class)
    public FlywayConfigurationCustomizer authzFlywayCustomizer() {
        return configuration -> {
            Location[] current = configuration.getLocations();
            String[] newLocations = new String[current.length + 1];
            for (int i = 0; i < current.length; i++) {
                newLocations[i] = current[i].getDescriptor();
            }
            newLocations[current.length] = "classpath:db/authz-migration";
            configuration.locations(newLocations);
        };
    }
}
