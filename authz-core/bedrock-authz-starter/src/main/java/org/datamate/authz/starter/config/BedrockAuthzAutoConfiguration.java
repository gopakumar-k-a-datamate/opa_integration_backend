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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

/**
 * Single entry point for Bedrock Authz auto-configuration.
 */
@AutoConfiguration(before = FlywayAutoConfiguration.class)
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "org.datamate.authz")
@EnableConfigurationProperties(OpaProperties.class)
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
}
