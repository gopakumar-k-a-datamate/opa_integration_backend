package org.datamate.pharmacy.infrastructure.listener;

import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.domain.model.User;
import org.datamate.identity.shared.event.user.UserCreatedEvent;
import org.datamate.identity.shared.event.user.UserRolesUpdatedEvent;
import org.datamate.identity.shared.model.UserStatus;
import org.datamate.pharmacy.adapter.out.persistence.projection.RoleProjectionRepository;
import org.datamate.pharmacy.adapter.out.persistence.projection.UserProjectionRepository;
import org.datamate.pharmacy.adapter.out.persistence.projection.entity.RoleProjectionJpaEntity;
import org.datamate.pharmacy.adapter.out.persistence.projection.entity.UserProjectionJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRoleProjectionEventListenerTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private UserProjectionRepository userProjectionRepository;

    @Mock
    private RoleProjectionRepository roleProjectionRepository;

    private UserRoleProjectionEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new UserRoleProjectionEventListener(
                userPersistencePort,
                userProjectionRepository,
                roleProjectionRepository
        );
    }

    @Test
    @DisplayName("Should project new user and roles into pharmacy read-model upon UserCreatedEvent")
    void testOnUserCreated_ProjectsUserAndRoles() {
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                1L,
                "pharmacist_sam",
                "sam@datamate.org",
                "9876543210",
                "Sam",
                "Wilson",
                UserStatus.ACTIVE,
                List.of("PHARMACIST"),
                "admin@123.com"
        );

        when(roleProjectionRepository.findByName("PHARMACIST")).thenReturn(Optional.empty());
        when(roleProjectionRepository.save(any(RoleProjectionJpaEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(userProjectionRepository.findById(userId)).thenReturn(Optional.empty());
        when(userProjectionRepository.save(any(UserProjectionJpaEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act - in-memory invocation
        listener.onUserCreated(event);

        // Assert Role projection
        ArgumentCaptor<RoleProjectionJpaEntity> roleCaptor = ArgumentCaptor.forClass(RoleProjectionJpaEntity.class);
        verify(roleProjectionRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getName()).isEqualTo("PHARMACIST");

        // Assert User projection
        ArgumentCaptor<UserProjectionJpaEntity> userCaptor = ArgumentCaptor.forClass(UserProjectionJpaEntity.class);
        verify(userProjectionRepository).save(userCaptor.capture());
        UserProjectionJpaEntity projectedUser = userCaptor.getValue();
        assertThat(projectedUser.getId()).isEqualTo(userId);
        assertThat(projectedUser.getUsername()).isEqualTo("pharmacist_sam");
        assertThat(projectedUser.getEmail()).isEqualTo("sam@datamate.org");
        assertThat(projectedUser.getStatus()).isEqualTo("ACTIVE");
        assertThat(projectedUser.getRoles()).hasSize(1);
        assertThat(projectedUser.getRoles().iterator().next().getName()).isEqualTo("PHARMACIST");
        assertThat(projectedUser.getChecksum()).isNotNull();
    }

    @Test
    @DisplayName("Should update user roles in pharmacy read-model upon UserRolesUpdatedEvent via UserPersistencePort")
    void testOnUserRolesUpdated_SyncsViaPort() {
        UUID userId = UUID.randomUUID();
        UserRolesUpdatedEvent event = new UserRolesUpdatedEvent(
                userId,
                2L,
                List.of("PHARMACIST", "SENIOR_PHARMACIST"),
                "IDENTITY_ADMIN"
        );

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(userId);
        when(mockUser.getUserName()).thenReturn("pharmacist_sam");
        when(mockUser.getEmail()).thenReturn("sam@datamate.org");
        when(mockUser.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(mockUser.getRoles()).thenReturn(List.of("PHARMACIST", "SENIOR_PHARMACIST"));

        when(userPersistencePort.findById(userId)).thenReturn(Optional.of(mockUser));

        RoleProjectionJpaEntity role1 = new RoleProjectionJpaEntity(UUID.randomUUID(), "PHARMACIST", "desc", 1L, "c1", OffsetDateTime.now());
        RoleProjectionJpaEntity role2 = new RoleProjectionJpaEntity(UUID.randomUUID(), "SENIOR_PHARMACIST", "desc", 1L, "c2", OffsetDateTime.now());
        when(roleProjectionRepository.findByName("PHARMACIST")).thenReturn(Optional.of(role1));
        when(roleProjectionRepository.findByName("SENIOR_PHARMACIST")).thenReturn(Optional.of(role2));

        UserProjectionJpaEntity existingEntity = new UserProjectionJpaEntity();
        existingEntity.setId(userId);
        existingEntity.setUsername("pharmacist_sam");
        existingEntity.setLastProcessedVersion(1L);
        when(userProjectionRepository.findById(userId)).thenReturn(Optional.of(existingEntity));

        // Act
        listener.onUserRolesUpdated(event);

        // Assert
        ArgumentCaptor<UserProjectionJpaEntity> userCaptor = ArgumentCaptor.forClass(UserProjectionJpaEntity.class);
        verify(userProjectionRepository).save(userCaptor.capture());
        UserProjectionJpaEntity saved = userCaptor.getValue();
        assertThat(saved.getRoles()).hasSize(2);
        assertThat(saved.getLastProcessedVersion()).isEqualTo(2L);
    }
}
