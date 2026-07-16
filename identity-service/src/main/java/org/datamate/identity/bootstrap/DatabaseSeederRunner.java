package org.datamate.identity.bootstrap;

import lombok.RequiredArgsConstructor;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DatabaseSeederRunner implements CommandLineRunner {
    @EnableLogger
    private Logger log;
    private final RoleStagingSeeder roleSeeder;
    private final UserStagingSeeder userSeeder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting Identity Service data seeding...");
        roleSeeder.seed();
        userSeeder.seed();
        log.info("Identity Service data seeding completed.");
    }
}
