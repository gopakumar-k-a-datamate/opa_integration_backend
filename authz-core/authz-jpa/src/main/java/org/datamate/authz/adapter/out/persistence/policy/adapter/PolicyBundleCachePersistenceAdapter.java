package org.datamate.authz.adapter.out.persistence.policy.adapter;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.adapter.out.persistence.policy.entity.PolicyBundleCacheJpaEntity;
import org.datamate.authz.adapter.out.persistence.policy.repository.SpringDataPolicyBundleCacheRepository;
import org.datamate.authz.application.port.out.policy.PolicyBundleCachePersistencePort;
import org.datamate.authz.adapter.out.persistence.policy.mapper.PolicyBundleCachePersistenceMapper;
import org.datamate.authz.domain.model.policy.entity.PolicyBundleCache;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PolicyBundleCachePersistenceAdapter implements PolicyBundleCachePersistencePort {

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



