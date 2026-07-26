package org.datamate.identity.domain.model;

import org.datamate.identity.domain.event.DomainEvent;
import org.datamate.identity.domain.event.UserCreatedEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserAndRegisterUserCreatedEvent() {
        User user = User.create(
                "john_doe",
                "john@example.com",
                "+1234567890",
                "hashedPassword",
                "John",
                "Doe",
                "admin"
        );

        assertNull(user.getId());
        assertEquals("john_doe", user.getUserName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("+1234567890", user.getPhoneNumber());
        assertEquals("hashedPassword", user.getPasswordHash());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("admin", user.getCreatedBy());

        List<DomainEvent> events = user.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof UserCreatedEvent);

        UserCreatedEvent createdEvent = (UserCreatedEvent) events.get(0);
        assertEquals("john_doe", createdEvent.userName());
        assertEquals("john@example.com", createdEvent.email());
        assertEquals("admin", createdEvent.createdBy());
    }
}
