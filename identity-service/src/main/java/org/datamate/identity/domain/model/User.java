package org.datamate.identity.domain.model;

import lombok.Getter;
import org.datamate.identity.shared.event.user.UserCreatedEvent;
import org.datamate.identity.shared.event.user.UserPasswordResetByAdminEvent;
import org.datamate.identity.shared.event.user.UserPasswordChangedEvent;
import org.datamate.identity.shared.event.user.UserInformationUpdatedEvent;
import org.datamate.identity.shared.model.UserStatus;
import com.datamate.bedrock.framework.common.ddd.domain.AggregateRoot;
import org.datamate.identity.domain.exception.user.InvalidUserDataException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class User extends AggregateRoot {
    private final UUID id;
    private final String userName;
    private final String email;
    private final String phoneNumber;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    private final String referenceSystem;
    private final String referenceValue;
    private final UserStatus status;
    private final List<String> roles;
    private final boolean passwordTemporary;
    private final Long version;
    private final String createdBy;
    private final LocalDateTime createdDate;
    private final String lastModifiedBy;
    private final LocalDateTime lastModifiedDate;

    private User(
            UUID id,
            String userName,
            String email,
            String phoneNumber,
            String passwordHash,
            String firstName,
            String lastName,
            String referenceSystem,
            String referenceValue,
            UserStatus status,
            List<String> roles,
            boolean passwordTemporary,
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
        this.referenceSystem = referenceSystem;
        this.referenceValue = referenceValue;
        this.status = status;
        this.roles = roles != null ? roles : new ArrayList<>();
        this.passwordTemporary = passwordTemporary;
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
            String referenceSystem,
            String referenceValue,
            String createdBy
    ) {
        validateState(userName, email, passwordHash, firstName, lastName, createdBy);

        UUID newUserId = UUID.randomUUID();

        User newUser = new User(
                newUserId,
                userName,
                email,
                phoneNumber,
                passwordHash,
                firstName,
                lastName,
                referenceSystem,
                referenceValue,
                UserStatus.INACTIVE,
                new ArrayList<>(),
                true,
                null,
                0L,
                createdBy,
                LocalDateTime.now(),
                createdBy,
                LocalDateTime.now()
        );

        newUser.registerEvent(new UserCreatedEvent(
                newUserId,
                newUser.getDomainVersion(),
                userName,
                email,
                phoneNumber,
                firstName,
                lastName,
                UserStatus.INACTIVE,
                createdBy
        ));

        return newUser;
    }

    public static User reconstitute(
            UUID id,
            String userName,
            String email,
            String phoneNumber,
            String passwordHash,
            String firstName,
            String lastName,
            String referenceSystem,
            String referenceValue,
            UserStatus status,
            List<String> roles,
            boolean passwordTemporary,
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
                referenceSystem,
                referenceValue,
                status,
                roles,
                passwordTemporary,
                version,
                domainVersion,
                createdBy,
                createdDate,
                lastModifiedBy,
                lastModifiedDate
        );
    }

    public User resetPassword(String newPasswordHash, String adminUsername) {
        User updatedUser = new User(
                this.id,
                this.userName,
                this.email,
                this.phoneNumber,
                newPasswordHash,
                this.firstName,
                this.lastName,
                this.referenceSystem,
                this.referenceValue,
                this.status,
                this.roles,
                true, // passwordTemporary = true
                this.version,
                this.getDomainVersion(),
                this.createdBy,
                this.createdDate,
                adminUsername,
                LocalDateTime.now()
        );
        updatedUser.registerEvent(new UserPasswordResetByAdminEvent(
                this.id,
                updatedUser.getDomainVersion() + 1,
                this.userName,
                this.email,
                this.phoneNumber,
                this.firstName,
                this.lastName,
                adminUsername
        ));
        return updatedUser;
    }

    public User changePassword(String newPasswordHash, String username) {
        User updatedUser = new User(
                this.id,
                this.userName,
                this.email,
                this.phoneNumber,
                newPasswordHash,
                this.firstName,
                this.lastName,
                this.referenceSystem,
                this.referenceValue,
                this.status,
                this.roles,
                false, // passwordTemporary = false
                this.version,
                this.getDomainVersion(),
                this.createdBy,
                this.createdDate,
                username,
                LocalDateTime.now()
        );
        updatedUser.registerEvent(new UserPasswordChangedEvent(
                this.id,
                updatedUser.getDomainVersion() + 1,
                this.userName,
                this.email,
                this.phoneNumber,
                this.firstName,
                this.lastName,
                username
        ));
        return updatedUser;
    }

    public User updateInformation(
            String email,
            String phoneNumber,
            String firstName,
            String lastName,
            String referenceSystem,
            String referenceValue,
            String adminUsername
    ) {
        validateUpdate(email, firstName, lastName);

        User updatedUser = new User(
                this.id,
                this.userName,
                email,
                phoneNumber,
                this.passwordHash,
                firstName,
                lastName,
                referenceSystem,
                referenceValue,
                this.status,
                this.roles,
                this.passwordTemporary,
                this.version,
                this.getDomainVersion(),
                this.createdBy,
                this.createdDate,
                adminUsername,
                LocalDateTime.now()
        );

        updatedUser.registerEvent(new UserInformationUpdatedEvent(
                this.id,
                updatedUser.getDomainVersion() + 1,
                email,
                phoneNumber,
                firstName,
                lastName,
                referenceSystem,
                referenceValue,
                adminUsername
        ));

        return updatedUser;
    }

    private void validateUpdate(String email, String firstName, String lastName) {
        if (email == null || email.isBlank()) {
            throw new InvalidUserDataException("Email is required.");
        }
        if (!email.contains("@")) {
            throw new InvalidUserDataException("Email must be valid.");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new InvalidUserDataException("First name is required.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new InvalidUserDataException("Last name is required.");
        }
    }

    private static void validateState(
            String userName,
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            String createdBy
    ) {
        if (userName == null || userName.isBlank()) {
            throw new InvalidUserDataException("Username is required.");
        }
        if (email == null || email.isBlank()) {
            throw new InvalidUserDataException("Email is required.");
        }
        if (!email.contains("@")) {
            throw new InvalidUserDataException("Email must be valid.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidUserDataException("Password hash is required.");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new InvalidUserDataException("First name is required.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new InvalidUserDataException("Last name is required.");
        }
        if (createdBy == null || createdBy.isBlank()) {
            throw new InvalidUserDataException("Created by reference is required.");
        }
    }
}
