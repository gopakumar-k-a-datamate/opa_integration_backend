package org.datamate.authz.application.port.out.policy;

import org.datamate.authz.domain.model.policy.entity.Resource;

import java.util.List;
import java.util.Optional;

/** Persistence operations for {@code authz_resource}. */
public interface ResourcePersistencePort {

    /** Insert or update a resource identified by {@code (namespace, name)}. */
    Resource upsert(Long id, String namespace, String name, String description);

    List<Resource> findAllActive();

    Optional<Resource> findByNamespaceAndName(String namespace, String name);
}


