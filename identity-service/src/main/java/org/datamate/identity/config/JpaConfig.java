package org.datamate.identity.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "org.datamate.identity")
@EntityScan(basePackages = {
        "org.datamate.identity",
        "com.datamate.bedrock"
})
public class JpaConfig {
}
