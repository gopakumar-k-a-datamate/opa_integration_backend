package org.datamate.authz.starter.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Standard configuration class to enable component scanning for authz-core and authz-opa components.
 * This is explicitly imported by BedrockAuthzAutoConfiguration to avoid using @ComponentScan
 * directly on an @AutoConfiguration class.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "org.datamate.authz")
public class AuthzComponentScanner {
}
