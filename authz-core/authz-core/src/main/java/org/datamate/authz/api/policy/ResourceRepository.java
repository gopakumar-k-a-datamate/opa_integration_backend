package org.datamate.authz.api.policy;

import org.datamate.authz.model.policy.entity.Resource;

import java.util.List;
import java.util.Optional;

/** Persistence operations for {@code authz_resource}. */
public interface ResourceRepository {

    /** Insert or update a resource identified by {@code (namespace, name)}. */
    Resource upsert(Long id, String namespace, String name, String description);

    Optional<Resource> findByNamespaceAndName(String namespace, String name);

    List<Resource> findAllActive();

}


