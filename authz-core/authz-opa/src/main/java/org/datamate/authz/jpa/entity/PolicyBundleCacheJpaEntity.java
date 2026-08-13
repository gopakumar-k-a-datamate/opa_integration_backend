package org.datamate.authz.jpa.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "authz_policy_bundle_cache")
public class PolicyBundleCacheJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String namespace;

    /** The compiled bundle.tar.gz binary data. */
    @Column(name = "bundle_data")
    private byte[] bundleData;

    /** MD5 hash of bundle_data — used as ETag for conditional OPA polling. */
    @Column
    private String etag;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PolicyBundleCacheJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getNamespace() {
        return namespace;
    }

    public byte[] getBundleData() {
        return bundleData;
    }

    public String getEtag() {
        return etag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setBundleData(byte[] bundleData) {
        this.bundleData = bundleData;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
