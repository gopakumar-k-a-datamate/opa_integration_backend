package org.datamate.pharmacy.adapter.out.persistence.entity;

import com.datamate.bedrock.framework.common.auditing.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity for the doctors table.
 * Extends BaseAuditableEntity for automatic audit fields
 * (createdDate, lastModifiedDate, createdBy, lastModifiedBy),
 * aligned with the dental project's entity convention.
 *
 * In Clean Architecture, JPA entities are an infrastructure concern and must
 * not leak into the Domain layer. Therefore, this class lives in the Adapter layer.
 */
@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
public class DoctorJpaEntity extends BaseAuditableEntity {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 100)
    private String department;

    @Column(nullable = false)
    private boolean active = true;
}
