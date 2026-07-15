package org.datamate.authz.adapter.out.persistence.policy.mapper;

import org.datamate.authz.adapter.out.persistence.policy.entity.PolicyBundleCacheJpaEntity;
import org.datamate.authz.domain.model.policy.entity.PolicyBundleCache;
import org.springframework.stereotype.Component;

@Component
public class PolicyBundleCachePersistenceMapper {
    public PolicyBundleCache toDomain(PolicyBundleCacheJpaEntity e) {
        if (e == null) return null;
        return new PolicyBundleCache(e.getId(), e.getBundleData(), e.getEtag(), e.getCreatedAt());
    }

    public void updateEntity(PolicyBundleCacheJpaEntity entity, byte[] bundleData, String etag) {
        entity.setBundleData(bundleData);
        entity.setEtag(etag);
    }
}
