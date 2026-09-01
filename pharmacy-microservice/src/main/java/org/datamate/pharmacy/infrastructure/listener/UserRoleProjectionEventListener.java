package org.datamate.pharmacy.infrastructure.listener;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.shared.event.user.*;
import org.datamate.pharmacy.adapter.out.persistence.projection.RoleProjectionRepository;
import org.datamate.pharmacy.adapter.out.persistence.projection.UserProjectionRepository;
import org.datamate.pharmacy.adapter.out.persistence.projection.entity.RoleProjectionJpaEntity;
import org.datamate.pharmacy.adapter.out.persistence.projection.entity.UserProjectionJpaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class UserRoleProjectionEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserRoleProjectionEventListener.class);

    private final UserPersistencePort userPersistencePort;
    private final UserProjectionRepository userProjectionRepository;
    private final RoleProjectionRepository roleProjectionRepository;

    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        log.info("Handling UserCreatedEvent for user '{}'", event.userName());
        syncUser(event.aggregateId(), event.userName(), event.email(), event.status().name(), event.roles());
    }

    @EventListener
    public void onUserRolesUpdated(UserRolesUpdatedEvent event) {
        log.info("Handling UserRolesUpdatedEvent for userId '{}'", event.aggregateId());
        userPersistencePort.findById(event.aggregateId()).ifPresent(user -> {
            syncUser(user.getId(), user.getUserName(), user.getEmail(), user.getStatus().name(), user.getRoles());
        });
    }

    @EventListener
    public void onUserInformationUpdated(UserInformationUpdatedEvent event) {
        log.info("Handling UserInformationUpdatedEvent for userId '{}'", event.aggregateId());
        userPersistencePort.findById(event.aggregateId()).ifPresent(user -> {
            syncUser(user.getId(), user.getUserName(), user.getEmail(), user.getStatus().name(), user.getRoles());
        });
    }

    @EventListener
    public void onUserActivated(UserActivatedEvent event) {
        log.info("Handling UserActivatedEvent for userId '{}'", event.aggregateId());
        userPersistencePort.findById(event.aggregateId()).ifPresent(user -> {
            syncUser(user.getId(), user.getUserName(), user.getEmail(), user.getStatus().name(), user.getRoles());
        });
    }

    @EventListener
    public void onUserDeactivated(UserDeactivatedEvent event) {
        log.info("Handling UserDeactivatedEvent for userId '{}'", event.aggregateId());
        userPersistencePort.findById(event.aggregateId()).ifPresent(user -> {
            syncUser(user.getId(), user.getUserName(), user.getEmail(), user.getStatus().name(), user.getRoles());
        });
    }

    private void syncUser(UUID userId, String username, String email, String status, List<String> roles) {
        try {
            // Process roles first to establish referential integrity
            Map<String, RoleProjectionJpaEntity> roleCache = new HashMap<>();
            if (roles != null) {
                for (String roleName : roles) {
                    RoleProjectionJpaEntity role = roleProjectionRepository.findByName(roleName).orElseGet(() -> {
                        RoleProjectionJpaEntity newRole = new RoleProjectionJpaEntity(
                                UUID.randomUUID(),
                                roleName,
                                "Projected Role from Identity Service",
                                1L,
                                computeMD5(roleName),
                                OffsetDateTime.now()
                        );
                        return roleProjectionRepository.save(newRole);
                    });
                    roleCache.put(roleName, role);
                }
            }

            // Sync User and Role mapping
            String calculatedChecksum = computeMD5(
                    username,
                    email,
                    status,
                    String.join(",", roles != null ? roles : Collections.emptyList())
            );

            UserProjectionJpaEntity user = userProjectionRepository.findById(userId).orElse(new UserProjectionJpaEntity());
            user.setId(userId);
            user.setUsername(username);
            user.setEmail(email);
            user.setStatus(status);
            user.setChecksum(calculatedChecksum);
            user.setLastReconciledAt(OffsetDateTime.now());
            user.setLastProcessedVersion(user.getLastProcessedVersion() == null ? 1L : user.getLastProcessedVersion() + 1);

            // Map roles
            Set<RoleProjectionJpaEntity> userRoles = new HashSet<>();
            if (roles != null) {
                for (String roleName : roles) {
                    RoleProjectionJpaEntity role = roleCache.get(roleName);
                    if (role != null) {
                        userRoles.add(role);
                    }
                }
            }
            user.setRoles(userRoles);
            userProjectionRepository.save(user);
            log.info("Successfully projected user '{}' to pharmacy module.", username);
        } catch (Exception e) {
            log.error("Failed to sync user projection: {}", e.getMessage(), e);
        }
    }

    private String computeMD5(String... inputs) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String combined = String.join(":", inputs);
            byte[] hash = md.digest(combined.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 calculation failed", e);
        }
    }
}
