package org.datamate.authz.adapter.out.persistence.policy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "authz_policy_bundle_cache")
@Getter
@Setter
@NoArgsConstructor
public class PolicyBundleCacheJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String namespace;

    /** The compiled bundle.tar.gz binary data. */
    @Lob
    @Column(name = "bundle_data", nullable = false)
    private byte[] bundleData;

    /** MD5 hash of bundle_data — used as ETag for conditional OPA polling. */
    @Column(nullable = false)
    private String etag;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

