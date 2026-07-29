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
        
        String commonPassword = passwordEncoder.encode("password");

        UUID adminId = insertUser("admin@123.com", commonPassword, "System", "Admin");
        UUID userId = insertUser("user@123.com", commonPassword, "Standard", "User");
        UUID managerId = insertUser("manager@123.com", commonPassword, "System", "Manager");
        UUID auditorId = insertUser("auditor@123.com", commonPassword, "System", "Auditor");
        UUID supportId = insertUser("support@123.com", commonPassword, "System", "Support");

        if (adminId != null) {
            insertUserRole(adminId, 1L); // ADMIN
            insertUserRole(adminId, 2L); // USER
        }
        if (userId != null) {
            insertUserRole(userId, 2L); // USER
        }
        if (managerId != null) {
            insertUserRole(managerId, 3L); // MANAGER
            insertUserRole(managerId, 2L); // USER
        }
        if (auditorId != null) {
            insertUserRole(auditorId, 4L); // AUDITOR
        }
        if (supportId != null) {
            insertUserRole(supportId, 5L); // SUPPORT
            insertUserRole(supportId, 2L); // USER
        }
    }

    private UUID insertUser(String userName, String passwordHash, String firstName, String lastName) {
        String checkSql = "SELECT id FROM users WHERE user_name = ?";
        try {
            UUID existingId = jdbcTemplate.queryForObject(checkSql, UUID.class, userName);
            log.info("User '{}' already exists. Skipping.", userName);
            return existingId;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // Does not exist, proceed to insert
        }

        UUID newUserId = UUID.randomUUID();
        String status = userName.startsWith("support") ? "INACTIVE" : "ACTIVE";
        String sql = "INSERT INTO users (id, user_name, email, password_hash, first_name, last_name, status, version, domain_version, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, 0, 1, ?, ?) RETURNING id";
        UUID newId = jdbcTemplate.queryForObject(sql, UUID.class, newUserId, userName, userName, passwordHash, firstName, lastName, status, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
        log.info("Inserted user '{}' with id {}.", userName, newId);
        return newId;
    }

    private void insertUserRole(UUID userId, Long roleId) {
        String checkSql = "SELECT COUNT(*) FROM user_roles WHERE user_id = ? AND role_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, roleId);
        if (count != null && count > 0) {
            return;
        }
        String sql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, roleId);
        log.info("Mapped user {} to role {}", userId, roleId);
    }
}
