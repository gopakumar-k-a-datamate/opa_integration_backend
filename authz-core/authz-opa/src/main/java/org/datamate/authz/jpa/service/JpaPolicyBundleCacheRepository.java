package org.datamate.authz.jpa.service;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.jpa.entity.PolicyBundleCacheJpaEntity;
import org.datamate.authz.jpa.repository.SpringDataPolicyBundleCacheRepository;
import org.datamate.authz.application.port.out.PolicyBundleCacheRepositoryPort;

import org.datamate.authz.model.policy.entity.PolicyBundleCache;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JpaPolicyBundleCacheRepository implements PolicyBundleCacheRepositoryPort {

    private final SpringDataPolicyBundleCacheRepository repository;

    @Override
    public Optional<PolicyBundleCache> getBundle(String namespace) {
        return repository.findByNamespace(namespace).map(this::toDomain);
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
                
        updateEntity(entity, namespace, bundleData, etag);
        return toDomain(repository.save(entity));
    }

    private PolicyBundleCache toDomain(PolicyBundleCacheJpaEntity e) {
        if (e == null) return null;
        return PolicyBundleCache.reconstitute(e.getId(), e.getNamespace(), e.getBundleData(), e.getEtag(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private void updateEntity(PolicyBundleCacheJpaEntity entity, String namespace, byte[] bundleData, String etag) {
        entity.setNamespace(namespace);
        entity.setBundleData(bundleData);
        entity.setEtag(etag);
    }

}



