package org.datamate.authz.starter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot auto-configuration for the REST endpoints of the {@code authz-core} library.
 *
 * <p>Can be disabled by setting {@code authz.rest.enabled=false}.</p>
 */
@AutoConfiguration(after = AuthzCoreAutoConfiguration.class)
@ConditionalOnClass(name = "org.datamate.authz.rest.controller.PolicyController")
@ConditionalOnProperty(prefix = "authz.rest", name = "enabled", matchIfMissing = true)
@ComponentScan(basePackages = "org.datamate.authz.rest")
public class AuthzRestAutoConfiguration {
}
