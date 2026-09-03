package org.datamate.identity.identity.bootstrap;

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

    public static final UUID SECURITY_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID MANAGER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID AUDITOR_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID SUPPORT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    public void seed() {
        log.info("Seeding Roles...");
        
        insertRole(SECURITY_ADMIN_ID, "SECURITY_ADMIN", "System & Security Administrator Role");
        insertRole(ADMIN_ID, "ADMIN", "Administrator Role");
        insertRole(USER_ID, "USER", "Standard User Role");
        insertRole(MANAGER_ID, "MANAGER", "Manager Role");
        insertRole(AUDITOR_ID, "AUDITOR", "Auditor Role");
        insertRole(SUPPORT_ID, "SUPPORT", "Support Role");
    }

    private void insertRole(UUID id, String name, String description) {
        String checkSql = "SELECT COUNT(*) FROM role WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, id);
        if (count != null && count > 0) {
            jdbcTemplate.update("UPDATE role SET status = 'ACTIVE' WHERE id = ? AND status = 'INACTIVE'", id);
            log.info("Role '{}' already exists (ensured ACTIVE).", name);
            return;
        }

        String sql = "INSERT INTO role (id, name, description, status, created_at, updated_at) VALUES (?, ?, ?, 'ACTIVE', ?, ?)";
        jdbcTemplate.update(sql, id, name, description, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
        log.info("Inserted role '{}' with status ACTIVE.", name);
    }
}
