package org.datamate.authz.api.policy;

import org.datamate.authz.model.policy.entity.Permission;

import java.util.List;
import java.util.Optional;

/** Persistence operations for {@code authz_permission}. */
public interface PermissionRepositoryPort {

    /** Insert or update a permission identified by {@code (resourceId, action)}. */
    Permission upsert(Long id, Long resourceId, String action, String code, String description);

    Optional<Permission> findByCode(String code);

    List<Permission> findAllActive();
}


