package org.datamate.authz.adapter.out.persistence.policy.mapper;

import org.datamate.authz.adapter.out.persistence.policy.entity.PolicyBundleCacheJpaEntity;
import org.datamate.authz.domain.model.policy.entity.PolicyBundleCache;
import org.springframework.stereotype.Component;

@Component
public class PolicyBundleCachePersistenceMapper {
    public PolicyBundleCache toDomain(PolicyBundleCacheJpaEntity e) {
        if (e == null) return null;
        return PolicyBundleCache.reconstitute(e.getId(), e.getNamespace(), e.getBundleData(), e.getEtag(), e.getCreatedAt(), e.getUpdatedAt());
    }

    public void updateEntity(PolicyBundleCacheJpaEntity entity, String namespace, byte[] bundleData, String etag) {
        entity.setNamespace(namespace);
        entity.setBundleData(bundleData);
        entity.setEtag(etag);
    }
}
