package org.datamate.identity.bootstrap;

import lombok.RequiredArgsConstructor;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserStagingSeeder {
    @EnableLogger
    private Logger log;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public void seed() {
        log.info("Seeding Users...");
        
        String adminPassword = passwordEncoder.encode("admin123");
        String userPassword = passwordEncoder.encode("user123");

        insertUser(UUID.fromString("10000000-0000-0000-0000-000000000001"), "admin@123.com", adminPassword, "System", "Admin");
        insertUser(UUID.fromString("10000000-0000-0000-0000-000000000002"), "user@123.com", userPassword, "Standard", "User");
    }

    private void insertUser(UUID id, String email, String passwordHash, String firstName, String lastName) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, email);
        if (count != null && count > 0) {
            log.info("User '{}' already exists. Skipping.", email);
            return;
        }

        String sql = "INSERT INTO users (id, email, password_hash, first_name, last_name, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, id, email, passwordHash, firstName, lastName, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
        log.info("Inserted user '{}'.", email);
    }
}
