package org.datamate.authz.adapter.out.persistence.policy.adapter;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.adapter.out.persistence.policy.entity.PolicyBundleCacheJpaEntity;
import org.datamate.authz.adapter.out.persistence.policy.repository.SpringDataPolicyBundleCacheRepository;
import org.datamate.authz.application.port.out.policy.PolicyBundleCachePersistencePort;
import org.datamate.authz.adapter.out.persistence.policy.mapper.PolicyBundleCachePersistenceMapper;
import org.datamate.authz.domain.model.policy.entity.PolicyBundleCache;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class PolicyBundleCachePersistenceAdapter implements PolicyBundleCachePersistencePort {

    private final SpringDataPolicyBundleCacheRepository repository;
    private final PolicyBundleCachePersistenceMapper mapper;
@Override
    public Optional<PolicyBundleCache> getBundle() {
        return repository.findFirstByOrderByCreatedAtDesc().map(mapper::toDomain);
    }

    @Override
    public PolicyBundleCache upsertBundle(byte[] bundleData, String etag) {
        // Fetch existing or create new if absent (avoiding dangerous deleteAll)
        PolicyBundleCacheJpaEntity entity = repository.findFirstByOrderByCreatedAtDesc()
                .orElseGet(() -> {
                    PolicyBundleCacheJpaEntity newEntity = new PolicyBundleCacheJpaEntity();
                    newEntity.setId(UUID.randomUUID());
                    return newEntity;
                });
                
        mapper.updateEntity(entity, bundleData, etag);
        return mapper.toDomain(repository.save(entity));
    }

}



