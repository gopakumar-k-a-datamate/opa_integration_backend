package org.datamate.authz.application.port.out.policy;

import org.datamate.authz.domain.model.policy.entity.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Persistence operations for {@code authz_permission}. */
public interface PermissionPersistencePort {

    /** Insert or update a permission identified by {@code (resourceId, action)}. */
    Permission upsert(UUID id, UUID resourceId, String action, String code, String description);

    List<Permission> findByResourceId(UUID resourceId);

    Optional<Permission> findByCode(String code);

    List<Permission> findAllActive();
}


