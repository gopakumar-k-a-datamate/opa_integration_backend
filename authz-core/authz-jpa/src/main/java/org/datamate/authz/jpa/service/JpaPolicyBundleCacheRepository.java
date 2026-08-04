package org.datamate.authz.jpa.service;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.jpa.entity.PolicyBundleCacheJpaEntity;
import org.datamate.authz.jpa.repository.SpringDataPolicyBundleCacheRepository;
import org.datamate.authz.api.policy.PolicyBundleCacheRepository;
import org.datamate.authz.jpa.mapper.PolicyBundleCachePersistenceMapper;
import org.datamate.authz.model.policy.entity.PolicyBundleCache;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JpaPolicyBundleCacheRepository implements PolicyBundleCacheRepository {

    private final SpringDataPolicyBundleCacheRepository repository;
    private final PolicyBundleCachePersistenceMapper mapper;

    @Override
    public Optional<PolicyBundleCache> getBundle(String namespace) {
        return repository.findByNamespace(namespace).map(mapper::toDomain);
    }

    @Override
    public PolicyBundleCache upsertBundle(String namespace, byte[] bundleData, String etag) {
        // Fetch existing or create new if absent (avoiding dangerous deleteAll)
        PolicyBundleCacheJpaEntity entity = repository.findByNamespace(namespace)
                .orElseGet(() -> {
                    PolicyBundleCacheJpaEntity newEntity = new PolicyBundleCacheJpaEntity();
                    newEntity.setId(null);
                    return newEntity;
                });
                
        mapper.updateEntity(entity, namespace, bundleData, etag);
        return mapper.toDomain(repository.save(entity));
    }

}



