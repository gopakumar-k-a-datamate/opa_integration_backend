package org.datamate.identity.domain.model;

import lombok.Getter;
import org.datamate.identity.domain.event.UserCreatedEvent;

import java.time.LocalDateTime;

@Getter
public class User extends AggregateRoot {
    private final Long id;
    private final String userName;
    private final String email;
    private final String phoneNumber;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    private final Long version;
    private final String createdBy;
    private final LocalDateTime createdDate;
    private final String lastModifiedBy;
    private final LocalDateTime lastModifiedDate;

    private User(
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
        super(domainVersion);
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.version = version;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
    }

    public static User create(
            String userName,
            String email,
            String phoneNumber,
            String passwordHash,
            String firstName,
            String lastName,
            String createdBy
    ) {
        User newUser = new User(
                null,
                userName,
                email,
                phoneNumber,
                passwordHash,
                firstName,
                lastName,
                0L,
                0L,
                createdBy,
                LocalDateTime.now(),
                createdBy,
                LocalDateTime.now()
        );

        newUser.registerEvent(new UserCreatedEvent(
                null,
                newUser.getDomainVersion(),
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
}
