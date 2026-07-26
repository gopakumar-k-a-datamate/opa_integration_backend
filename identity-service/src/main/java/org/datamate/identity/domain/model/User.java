package org.datamate.identity.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.domain.event.DomainEvent;
import org.datamate.identity.domain.event.UserCreatedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    private final Long id;
    private final String userName;
    private final String email;
    private final String phoneNumber;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    private final Long version;
    private final Long domainVersion;
    private final String createdBy;
    private final LocalDateTime createdDate;
    private final String lastModifiedBy;
    private final LocalDateTime lastModifiedDate;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public static User create(
            String userName,
            String email,
            String phoneNumber,
            String passwordHash,
            String firstName,
            String lastName,
            String createdBy
    ) {
        Long initialDomainVersion = 1L;
        User newUser = new User(
                null,
                userName,
                email,
                phoneNumber,
                passwordHash,
                firstName,
                lastName,
                0L,
                initialDomainVersion,
                createdBy,
                LocalDateTime.now(),
                createdBy,
                LocalDateTime.now()
        );

        newUser.registerEvent(new UserCreatedEvent(
                null,
                initialDomainVersion,
                userName,
                email,
                phoneNumber,
                firstName,
                lastName,
                createdBy
        ));

        return newUser;
    }

    public static User reconstitute(
            Long id,
            String userName,
            String email,
            String phoneNumber,
            String passwordHash,
            String firstName,
            String lastName,
            Long version,
            Long domainVersion,
            String createdBy,
            LocalDateTime createdDate,
            String lastModifiedBy,
            LocalDateTime lastModifiedDate
    ) {
        return new User(
                id,
                userName,
                email,
                phoneNumber,
                passwordHash,
                firstName,
                lastName,
                version,
                domainVersion,
                createdBy,
                createdDate,
                lastModifiedBy,
                lastModifiedDate
        );
    }

    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
