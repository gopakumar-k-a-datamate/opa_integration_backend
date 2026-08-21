package org.datamate.pharmacy.application.scheduler;

import org.datamate.pharmacy.adapter.out.persistence.projection.RoleProjectionRepository;
import org.datamate.pharmacy.adapter.out.persistence.projection.UserProjectionRepository;
import org.datamate.pharmacy.adapter.out.persistence.projection.entity.RoleProjectionJpaEntity;
import org.datamate.pharmacy.adapter.out.persistence.projection.entity.UserProjectionJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserRoleProjectionSchedulerTest {

    static {
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Autowired
    private UserRoleProjectionScheduler scheduler;

    @Autowired
    private UserProjectionRepository userProjectionRepository;

    @Autowired
    private RoleProjectionRepository roleProjectionRepository;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        userProjectionRepository.deleteAll();
        roleProjectionRepository.deleteAll();
    }

    @Test
    public void testSyncProjections_NewUserAndRoleCreated() {
        // 1. Mock Login Auth Response
        UserRoleProjectionScheduler.AuthResponse authResponse = new UserRoleProjectionScheduler.AuthResponse(
                "mock-token", "mock-refresh", "admin", "admin@123.com"
        );
        when(restTemplate.postForEntity(anyString(), any(), eq(UserRoleProjectionScheduler.AuthResponse.class)))
                .thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        // 2. Mock Fetch Users Response (1 User with Role "PHARMACIST")
        UUID userId = UUID.randomUUID();
        UserRoleProjectionScheduler.UserDto userDto = new UserRoleProjectionScheduler.UserDto(
                userId, "pharmacist_jane", "jane@pharmacy.com", "ACTIVE", List.of("PHARMACIST")
        );
        UserRoleProjectionScheduler.PagedUserResponse pagedResponse = new UserRoleProjectionScheduler.PagedUserResponse(
                List.of(userDto), 1, 10, 1L, 1
        );

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRoleProjectionScheduler.PagedUserResponse.class)
        )).thenReturn(new ResponseEntity<>(pagedResponse, HttpStatus.OK));

        // 3. Trigger Scheduler
        scheduler.syncProjections();

        // 4. Verify Local Projections DB State
        Optional<RoleProjectionJpaEntity> roleOpt = roleProjectionRepository.findByName("PHARMACIST");
        assertTrue(roleOpt.isPresent(), "Role PHARMACIST should be projected");
        assertNotNull(roleOpt.get().getChecksum());
        assertEquals(1L, roleOpt.get().getLastProcessedVersion());

        Optional<UserProjectionJpaEntity> userOpt = userProjectionRepository.findById(userId);
        assertTrue(userOpt.isPresent(), "User should be projected");
        UserProjectionJpaEntity userEntity = userOpt.get();
        assertEquals("pharmacist_jane", userEntity.getUsername());
        assertEquals("ACTIVE", userEntity.getStatus());
        assertEquals(1L, userEntity.getLastProcessedVersion());
        assertNotNull(userEntity.getChecksum());
        assertNotNull(userEntity.getLastReconciledAt());

        assertEquals(1, userEntity.getRoles().size());
        assertEquals("PHARMACIST", userEntity.getRoles().iterator().next().getName());
    }

    @Test
    public void testSyncProjections_IncrementalVersionAndChecksumDrift() {
        // 1. Seed user in DB
        UUID userId = UUID.randomUUID();
        RoleProjectionJpaEntity pharmacistRole = roleProjectionRepository.save(new RoleProjectionJpaEntity(
                UUID.randomUUID(), "PHARMACIST", "Mock description", 1L, "some-checksum", null
        ));
        
        UserProjectionJpaEntity userEntity = new UserProjectionJpaEntity(
                userId, "pharmacist_jane", "jane@pharmacy.com", "ACTIVE", Set.of(pharmacistRole), 1L, "old-checksum", null
        );
        userProjectionRepository.save(userEntity);

        // 2. Mock Login Auth Response
        UserRoleProjectionScheduler.AuthResponse authResponse = new UserRoleProjectionScheduler.AuthResponse(
                "mock-token", "mock-refresh", "admin", "admin@123.com"
        );
        when(restTemplate.postForEntity(anyString(), any(), eq(UserRoleProjectionScheduler.AuthResponse.class)))
                .thenReturn(new ResponseEntity<>(authResponse, HttpStatus.OK));

        // 3. Mock Fetch Users (User role updated to List.of("PHARMACIST", "SENIOR_PHARMACIST"))
        UserRoleProjectionScheduler.UserDto updatedUserDto = new UserRoleProjectionScheduler.UserDto(
                userId, "pharmacist_jane", "jane@pharmacy.com", "ACTIVE", List.of("PHARMACIST", "SENIOR_PHARMACIST")
        );
        UserRoleProjectionScheduler.PagedUserResponse pagedResponse = new UserRoleProjectionScheduler.PagedUserResponse(
                List.of(updatedUserDto), 1, 10, 1L, 1
        );

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRoleProjectionScheduler.PagedUserResponse.class)
        )).thenReturn(new ResponseEntity<>(pagedResponse, HttpStatus.OK));

        // 4. Trigger Scheduler
        scheduler.syncProjections();

        // 5. Verify user is updated, version incremented, and role mapping expanded
        Optional<UserProjectionJpaEntity> updatedUserOpt = userProjectionRepository.findById(userId);
        assertTrue(updatedUserOpt.isPresent());
        UserProjectionJpaEntity resultUser = updatedUserOpt.get();

        assertEquals(2L, resultUser.getLastProcessedVersion(), "Version should increment on role list changes");
        assertNotEquals("old-checksum", resultUser.getChecksum());
        assertEquals(2, resultUser.getRoles().size(), "Should have both PHARMACIST and SENIOR_PHARMACIST mappings");
    }
}
