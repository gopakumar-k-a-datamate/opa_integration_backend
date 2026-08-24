package org.datamate.identity.role.domain.model;

import com.datamate.bedrock.framework.common.ddd.event.DomainEvent;
import org.datamate.identity.role.shared.event.role.RoleActivatedEvent;
import org.datamate.identity.role.shared.event.role.RoleDeactivatedEvent;
import org.datamate.identity.role.shared.model.RoleStatus;
import org.datamate.identity.role.domain.exception.InvalidRoleDataException;
import com.datamate.bedrock.framework.common.ddd.datatype.EntityReference;
import com.datamate.bedrock.framework.common.ddd.datatype.ResourceIdentifier;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    private final UUID roleId = UUID.randomUUID();
    private final EntityReference<UUID> adminUserRef = new EntityReference<>(
            UUID.randomUUID(),
            new ResourceIdentifier("identity-service", "admin")
    );

    @Test
    void shouldActivateRoleWhenInactive() {
        Role role = Role.reconstitute(
                roleId,
                "TEST_ROLE",
                "Description",
                RoleStatus.INACTIVE,
                null,
                null,
                1L,
                1L,
                adminUserRef,
                LocalDateTime.now(),
                adminUserRef,
                LocalDateTime.now()
        );

        Role activatedRole = role.activate(adminUserRef);

        assertEquals(RoleStatus.ACTIVE, activatedRole.getStatus());
        assertEquals(adminUserRef, activatedRole.getLastModifiedBy());
        assertEquals(role.getDomainVersion() + 1, activatedRole.getDomainVersion());

        List<DomainEvent> events = activatedRole.pullEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof RoleActivatedEvent);

        RoleActivatedEvent event = (RoleActivatedEvent) events.get(0);
        assertEquals(roleId, event.aggregateId());
        assertEquals("TEST_ROLE", event.name());
        assertEquals(adminUserRef, event.activatedBy());
    }

    @Test
    void shouldThrowInvalidRoleDataExceptionWhenAlreadyActive() {
        Role role = Role.reconstitute(
                roleId,
                "TEST_ROLE",
                "Description",
                RoleStatus.ACTIVE,
                null,
                null,
                1L,
                1L,
                adminUserRef,
                LocalDateTime.now(),
                adminUserRef,
                LocalDateTime.now()
        );

        assertThrows(InvalidRoleDataException.class, () -> role.activate(adminUserRef));
    }

    @Test
    void shouldDeactivateRoleWhenActive() {
        Role role = Role.reconstitute(
                roleId,
                "TEST_ROLE",
                "Description",
                RoleStatus.ACTIVE,
                null,
                null,
                1L,
                1L,
                adminUserRef,
                LocalDateTime.now(),
                adminUserRef,
                LocalDateTime.now()
        );

        Role deactivatedRole = role.deactivate(adminUserRef);

        assertEquals(RoleStatus.INACTIVE, deactivatedRole.getStatus());
        assertEquals(adminUserRef, deactivatedRole.getLastModifiedBy());
        assertEquals(role.getDomainVersion() + 1, deactivatedRole.getDomainVersion());

        List<DomainEvent> events = deactivatedRole.pullEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof RoleDeactivatedEvent);

        RoleDeactivatedEvent event = (RoleDeactivatedEvent) events.get(0);
        assertEquals(roleId, event.aggregateId());
        assertEquals("TEST_ROLE", event.name());
        assertEquals(adminUserRef, event.deactivatedBy());
    }

    @Test
    void shouldThrowInvalidRoleDataExceptionWhenAlreadyInactive() {
        Role role = Role.reconstitute(
                roleId,
                "TEST_ROLE",
                "Description",
                RoleStatus.INACTIVE,
                null,
                null,
                1L,
                1L,
                adminUserRef,
                LocalDateTime.now(),
                adminUserRef,
                LocalDateTime.now()
        );

        assertThrows(InvalidRoleDataException.class, () -> role.deactivate(adminUserRef));
    }
}


