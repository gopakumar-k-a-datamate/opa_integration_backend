package org.datamate.authz.adapter.out.persistence.policy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "authz_permission",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"resource_id", "action"}),
                @UniqueConstraint(columnNames = {"code"})
        })
@Getter
@Setter
@NoArgsConstructor
public class PermissionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(nullable = false)
    private String action;

    /** Auto-generated: {namespace}:{resource}:{action} */
    @Column(nullable = false, unique = true)
    private String code;

    @Column
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

