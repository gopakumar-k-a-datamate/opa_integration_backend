package org.datamate.pharmacy.adapter.out.persistence.projection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "projection_role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleProjectionJpaEntity {

    @Id
    private UUID id;
    
    private String name;
    
    private String description;

    @Column(name = "last_processed_version")
    private Long lastProcessedVersion;

    private String checksum;

    @Column(name = "last_reconciled_at")
    private OffsetDateTime lastReconciledAt;
}
