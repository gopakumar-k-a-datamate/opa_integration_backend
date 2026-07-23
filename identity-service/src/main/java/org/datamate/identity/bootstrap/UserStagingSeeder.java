package org.datamate.identity.bootstrap;

import lombok.RequiredArgsConstructor;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

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

        Long adminId = insertUser("admin@123.com", commonPassword, "System", "Admin");
        Long userId = insertUser("user@123.com", commonPassword, "Standard", "User");
        Long managerId = insertUser("manager@123.com", commonPassword, "System", "Manager");
        Long auditorId = insertUser("auditor@123.com", commonPassword, "System", "Auditor");
        Long supportId = insertUser("support@123.com", commonPassword, "System", "Support");

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

    private Long insertUser(String userName, String passwordHash, String firstName, String lastName) {
        String checkSql = "SELECT id FROM users WHERE user_name = ?";
        try {
            Long existingId = jdbcTemplate.queryForObject(checkSql, Long.class, userName);
            log.info("User '{}' already exists. Skipping.", userName);
            return existingId;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // Does not exist, proceed to insert
        }

        String sql = "INSERT INTO users (user_name, password_hash, first_name, last_name, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        Long newId = jdbcTemplate.queryForObject(sql, Long.class, userName, passwordHash, firstName, lastName, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
        log.info("Inserted user '{}' with id {}.", userName, newId);
        return newId;
    }

    private void insertUserRole(Long userId, Long roleId) {
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
