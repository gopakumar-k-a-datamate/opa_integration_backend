package org.datamate.authz.adapter.out.persistence.policy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;
import org.datamate.authz.domain.model.policy.enumtype.FieldStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "authz_condition_field",
        uniqueConstraints = @UniqueConstraint(columnNames = {"permission_id", "field_name"}))
@Getter
@Setter
@NoArgsConstructor
public class ConditionFieldJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false)
    private FieldType fieldType;

    @Column(name = "display_name")
    private String displayName;

    /** JSON array string, e.g. {@code ["CASH","HDFC","SBI"]}. */
    @Column(name = "allowed_values", columnDefinition = "text")
    private String allowedValues;

    @Column(name = "options_endpoint")
    private String optionsEndpoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldStatus status = FieldStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

