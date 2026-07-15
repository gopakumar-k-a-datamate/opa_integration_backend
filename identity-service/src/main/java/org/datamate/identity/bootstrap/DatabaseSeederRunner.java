package org.datamate.identity.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeederRunner implements CommandLineRunner {
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
