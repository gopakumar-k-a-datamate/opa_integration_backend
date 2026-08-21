package org.datamate.pharmacy.application.scheduler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.datamate.pharmacy.adapter.out.persistence.projection.RoleProjectionRepository;
import org.datamate.pharmacy.adapter.out.persistence.projection.UserProjectionRepository;
import org.datamate.pharmacy.adapter.out.persistence.projection.entity.RoleProjectionJpaEntity;
import org.datamate.pharmacy.adapter.out.persistence.projection.entity.UserProjectionJpaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class UserRoleProjectionScheduler {

    private static final Logger log = LoggerFactory.getLogger(UserRoleProjectionScheduler.class);

    private final RestTemplate restTemplate;
    private final UserProjectionRepository userProjectionRepository;
    private final RoleProjectionRepository roleProjectionRepository;

    @Value("${app.identity.url:http://localhost:8085}")
    private String identityUrl;

    @Scheduled(fixedDelayString = "${app.projection.sync-delay-ms:15000}")
    public void syncProjections() {
        log.info("Starting User and Role projections sync from identity service...");
        try {
            // 1. Authenticate with Identity Service to get JWT Token
            String token = loginAsAdmin();
            if (token == null) {
                log.warn("Failed to retrieve admin token. Skipping projection sync.");
                return;
            }

            // 2. Query all users from Identity Service
            List<UserDto> users = fetchUsers(token);
            if (users == null) {
                log.warn("Fetch users returned null. Skipping sync.");
                return;
            }

            // 3. Process roles first to establish referential integrity
            Map<String, RoleProjectionJpaEntity> roleCache = new HashMap<>();
            for (UserDto userDto : users) {
                if (userDto.getRoles() != null) {
                    for (String roleName : userDto.getRoles()) {
                        roleCache.computeIfAbsent(roleName, name -> {
                            Optional<RoleProjectionJpaEntity> existing = roleProjectionRepository.findByName(name);
                            if (existing.isPresent()) {
                                RoleProjectionJpaEntity roleEntity = existing.get();
                                // Recalculate checksum
                                String expectedChecksum = computeMD5(name);
                                if (!expectedChecksum.equals(roleEntity.getChecksum())) {
                                    roleEntity.setChecksum(expectedChecksum);
                                    roleEntity.setLastReconciledAt(OffsetDateTime.now());
                                    roleEntity.setLastProcessedVersion(roleEntity.getLastProcessedVersion() + 1);
                                    return roleProjectionRepository.save(roleEntity);
                                }
                                return roleEntity;
                            } else {
                                RoleProjectionJpaEntity newRole = new RoleProjectionJpaEntity(
                                        UUID.randomUUID(), 
                                        name, 
                                        "Projected Role from Identity Service",
                                        1L,
                                        computeMD5(name),
                                        OffsetDateTime.now()
                                );
                                return roleProjectionRepository.save(newRole);
                            }
                        });
                    }
                }
            }

            // 4. Sync Users and Role mappings
            for (UserDto userDto : users) {
                Optional<UserProjectionJpaEntity> existingUserOpt = userProjectionRepository.findById(userDto.getId());
                UserProjectionJpaEntity user;
                
                String calculatedChecksum = computeMD5(
                        userDto.getUserName(), 
                        userDto.getEmail(), 
                        userDto.getStatus(), 
                        String.join(",", userDto.getRoles() != null ? userDto.getRoles() : Collections.emptyList())
                );

                if (existingUserOpt.isPresent()) {
                    user = existingUserOpt.get();
                    
                    // Check if anything has changed before saving to optimize DB updates
                    if (!calculatedChecksum.equals(user.getChecksum())) {
                        user.setUsername(userDto.getUserName());
                        user.setEmail(userDto.getEmail());
                        user.setStatus(userDto.getStatus());
                        user.setChecksum(calculatedChecksum);
                        user.setLastReconciledAt(OffsetDateTime.now());
                        user.setLastProcessedVersion(user.getLastProcessedVersion() + 1);
                    } else {
                        // Skip updating if checksum matches
                        continue;
                    }
                } else {
                    user = new UserProjectionJpaEntity();
                    user.setId(userDto.getId());
                    user.setUsername(userDto.getUserName());
                    user.setEmail(userDto.getEmail());
                    user.setStatus(userDto.getStatus());
                    user.setChecksum(calculatedChecksum);
                    user.setLastReconciledAt(OffsetDateTime.now());
                    user.setLastProcessedVersion(1L);
                }

                // Map roles
                Set<RoleProjectionJpaEntity> userRoles = new HashSet<>();
                if (userDto.getRoles() != null) {
                    for (String roleName : userDto.getRoles()) {
                        RoleProjectionJpaEntity role = roleCache.get(roleName);
                        if (role != null) {
                            userRoles.add(role);
                        }
                    }
                }
                user.setRoles(userRoles);
                userProjectionRepository.save(user);
            }

            log.info("Successfully synced and reconciled user projections from identity service.");

        } catch (Exception e) {
            log.error("Error occurred during user and role projection sync: {}", e.getMessage(), e);
        }
    }

    private String loginAsAdmin() {
        try {
            String loginUrl = identityUrl + "/api/v1/auth/login";
            Map<String, String> request = new HashMap<>();
            request.put("userName", "admin@123.com");
            request.put("password", "password");

            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(loginUrl, request, AuthResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getAccessToken();
            }
        } catch (Exception e) {
            log.error("Failed to authenticate with identity service: {}", e.getMessage());
        }
        return null;
    }

    private List<UserDto> fetchUsers(String token) {
        try {
            String usersUrl = identityUrl + "/api/v1/users?page=1&size=1000";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<PagedUserResponse> response = restTemplate.exchange(
                    usersUrl, HttpMethod.GET, entity, PagedUserResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getContent();
            }
        } catch (Exception e) {
            log.error("Failed to fetch users from identity service: {}", e.getMessage());
        }
        return null;
    }

    private String computeMD5(String... inputs) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            StringBuilder sb = new StringBuilder();
            for (String input : inputs) {
                sb.append(input != null ? input : "null").append("|");
            }
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(Objects.hash((Object[]) inputs));
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String userName;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private UUID id;
        private String userName;
        private String email;
        private String status;
        private List<String> roles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagedUserResponse {
        private List<UserDto> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
    }
}
