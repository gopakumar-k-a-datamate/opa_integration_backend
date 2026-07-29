package org.datamate.identity.domain.model;

import lombok.Getter;
import org.datamate.identity.shared.event.user.UserCreatedEvent;
import org.datamate.identity.shared.model.UserStatus;
import com.datamate.bedrock.framework.common.ddd.domain.AggregateRoot;

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
                UserStatus.ACTIVE,
                new ArrayList<>(),
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
                UserStatus.ACTIVE,
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
                version,
                domainVersion,
                createdBy,
                createdDate,
                lastModifiedBy,
                lastModifiedDate
        );
    }
}
