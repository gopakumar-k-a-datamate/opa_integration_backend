package org.datamate.authz.starter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot auto-configuration entry point for the {@code authz-core} library.
 *
 * <p>This class only configures the core authorization engine components,
 * ensuring no web or persistence beans are scanned unconditionally.</p>
 */
@AutoConfiguration
@org.springframework.context.annotation.EnableAspectJAutoProxy
@ComponentScan(basePackages = {
        "org.datamate.authz.service",
        "org.datamate.authz.compiler",
        "org.datamate.authz.dto",
        "org.datamate.authz.adapter.out.opa",
        "org.datamate.authz.starter.enforcement"
})
public class AuthzCoreAutoConfiguration {
}
