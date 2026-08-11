package org.datamate.identity.domain.model;

import lombok.Getter;
import org.datamate.identity.shared.model.RoleStatus;
import org.datamate.identity.domain.exception.role.InvalidRoleDataException;
import com.datamate.bedrock.framework.common.ddd.domain.AggregateRoot;
import com.datamate.bedrock.framework.common.ddd.datatype.EntityReference;

import java.time.LocalDateTime;
import java.util.UUID;

import org.datamate.identity.shared.event.role.RoleCreatedEvent;

@Getter
public class Role extends AggregateRoot {
    private final UUID id;
    private final String name;
    private final String description;
    private final RoleStatus status;
    private final String referenceSystem;
    private final String referenceValue;
    private final Long version;
    private final EntityReference<UUID> createdBy;
    private final LocalDateTime createdDate;
    private final EntityReference<UUID> lastModifiedBy;
    private final LocalDateTime lastModifiedDate;

    private Role(
            UUID id,
            String name,
            String description,
            RoleStatus status,
            String referenceSystem,
            String referenceValue,
            Long version,
            Long domainVersion,
            EntityReference<UUID> createdBy,
            LocalDateTime createdDate,
            EntityReference<UUID> lastModifiedBy,
            LocalDateTime lastModifiedDate
    ) {
        super(domainVersion);
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.referenceSystem = referenceSystem;
        this.referenceValue = referenceValue;
        this.version = version;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
    }

    public static Role create(
            String name,
            String description,
            EntityReference<UUID> createdBy
    ) {
        validateState(name, RoleStatus.INACTIVE, createdBy);

        return new Role(
                null,
                name,
                description,
                RoleStatus.INACTIVE,
                null,
                null,
                null,
                0L,
                createdBy,
                LocalDateTime.now(),
                createdBy,
                LocalDateTime.now()
        );
    }

    public static Role reconstitute(
            UUID id,
            String name,
            String description,
            RoleStatus status,
            String referenceSystem,
            String referenceValue,
            Long version,
            Long domainVersion,
            EntityReference<UUID> createdBy,
            LocalDateTime createdDate,
            EntityReference<UUID> lastModifiedBy,
            LocalDateTime lastModifiedDate
    ) {
        return new Role(
                id,
                name,
                description,
                status,
                referenceSystem,
                referenceValue,
                version,
                domainVersion,
                createdBy,
                createdDate,
                lastModifiedBy,
                lastModifiedDate
        );
    }

    private static void validateState(String name, RoleStatus status, EntityReference<UUID> createdBy) {
        if (name == null || name.isBlank()) {
            throw new InvalidRoleDataException("role.validation.name.required", "Role name is mandatory.");
        }
        if (status == null) {
            throw new InvalidRoleDataException("role.validation.status.required", "Role status is mandatory.");
        }
        if (createdBy == null) {
            throw new InvalidRoleDataException("role.validation.createdBy.required", "Created by reference is required.");
        }
    }

    public Role publishCreationEvent() {
        this.registerEvent(new RoleCreatedEvent(
                this.id,
                this.getDomainVersion(),
                this.name,
                this.description,
                this.status,
                this.createdBy
        ));
        return this;
    }
}
