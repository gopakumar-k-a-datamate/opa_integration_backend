package org.datamate.identity.bootstrap;

import lombok.RequiredArgsConstructor;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoleStagingSeeder {
    @EnableLogger
    private Logger log;
    private final JdbcTemplate jdbcTemplate;

    public void seed() {
        log.info("Seeding Roles...");
        
        insertRole(UUID.fromString("00000000-0000-0000-0000-000000000001"), "ADMIN", "Administrator Role");
        insertRole(UUID.fromString("00000000-0000-0000-0000-000000000002"), "USER", "Standard User Role");
    }

    private void insertRole(UUID id, String name, String description) {
        String checkSql = "SELECT COUNT(*) FROM role WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, id);
        if (count != null && count > 0) {
            log.info("Role '{}' already exists. Skipping.", name);
            return;
        }

        String sql = "INSERT INTO role (id, name, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, id, name, description, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
        log.info("Inserted role '{}'.", name);
    }
}
