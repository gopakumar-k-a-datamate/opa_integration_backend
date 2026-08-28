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
import org.datamate.authz.api.principal.PrincipalProvider;
import org.datamate.authz.enforcement.DefaultPrincipalProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.datamate.authz.client.config.OpaProperties;
import org.datamate.authz.client.OpaPolicyValidator;
import org.datamate.authz.rest.client.RestPolicyEvaluationClient;
import org.datamate.authz.api.policy.PolicyEvaluationClient;
import org.datamate.authz.api.policy.PolicyValidation;
import org.datamate.authz.api.endpoint.AuthzBeans;
import org.datamate.authz.api.endpoint.EndpointAuthorization;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Single entry point for Bedrock Authz auto-configuration.
 */
@AutoConfiguration(before = FlywayAutoConfiguration.class)
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "org.datamate.authz")
@EnableConfigurationProperties({ OpaProperties.class, BundleProperties.class })
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
    public static class PrincipalProviderConfiguration {
        @Bean
        @ConditionalOnMissingBean(PrincipalProvider.class)
        public PrincipalProvider defaultPrincipalProvider() {
            return new DefaultPrincipalProvider();
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class OpaConfiguration {

        @Bean
        @ConditionalOnMissingBean(PolicyValidation.class)
        public PolicyValidation opaPolicyValidator(RestTemplate restTemplate, ObjectMapper objectMapper, OpaProperties opaProperties) {
            return new OpaPolicyValidator(restTemplate, objectMapper, opaProperties.getValidationUrl());
        }

        @Bean
        @ConditionalOnMissingBean(PolicyEvaluationClient.class)
        public PolicyEvaluationClient restPolicyEvaluationClient(RestTemplateBuilder restTemplateBuilder, OpaProperties opaProperties) {
            return new RestPolicyEvaluationClient(restTemplateBuilder, opaProperties.getEvaluationUrl());
        }
    }

    /**
     * Auto-activates the {@code BundleController} so that consumers do not need to
     * define their own {@code @Bean(AuthzBeans.BUNDLE) EndpointAuthorization}.
     *
     * <p>Behaviour:</p>
     * <ul>
     *   <li>Enabled by default. Disable with {@code datamate.authz.bundle.enabled=false}.</li>
     *   <li>If the consumer defines their own {@code @Bean(AuthzBeans.BUNDLE)}, this
     *       auto-configured bean backs off via {@code @ConditionalOnMissingBean}.</li>
     *   <li>If {@code datamate.authz.bundle.api-key} is set, every request must include
     *       {@code Authorization: Bearer <api-key>}. Otherwise the endpoint is open.</li>
     * </ul>
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "datamate.authz.bundle", name = "enabled", matchIfMissing = true)
    public static class BundleEndpointConfiguration {

        @Bean(AuthzBeans.BUNDLE)
        @ConditionalOnMissingBean(name = AuthzBeans.BUNDLE)
        public EndpointAuthorization bundleAuthorization(BundleProperties bundleProperties) {
            return context -> {
                String apiKey = bundleProperties.getApiKey();
                if (apiKey != null && !apiKey.isBlank()) {
                    HttpServletRequest request = ((ServletRequestAttributes)
                            RequestContextHolder.currentRequestAttributes()).getRequest();
                    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        throw new AccessDeniedException(
                                "Bundle endpoint requires Bearer token authentication");
                    }

                    String token = authHeader.substring(7);
                    if (!apiKey.equals(token)) {
                        throw new AccessDeniedException("Invalid bundle API key");
                    }
                }
            };
        }
    }
}
