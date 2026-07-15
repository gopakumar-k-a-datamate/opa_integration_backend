package org.datamate.authz.shared.config;

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
 *
 * <p><b>Flyway:</b> The host application must include the authz-core migration location
 * in its {@code application.properties}:</p>
 * <pre>{@code
 * spring.flyway.locations=classpath:db/migration,classpath:db/migration/authz-core
 * }</pre>
 */
@AutoConfiguration
@ComponentScan(basePackages = {
        "org.datamate.authz.adapter",
        "org.datamate.authz.application",
        "org.datamate.authz.domain.service.policy"
})
@EnableJpaRepositories(basePackages = "org.datamate.authz.adapter.out.persistence.policy.repository")
@EntityScan(basePackages = "org.datamate.authz.adapter.out.persistence.policy.entity")
@EnableTransactionManagement
public class AuthzCoreAutoConfiguration {
}


