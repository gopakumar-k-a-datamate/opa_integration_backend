package org.datamate.pharmacy.adapter.out.persistence.projection.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "projection_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProjectionJpaEntity {

    @Id
    private UUID id;
    
    private String username;
    
    private String email;
    
    private String status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "projection_user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleProjectionJpaEntity> roles = new HashSet<>();

    @Column(name = "last_processed_version")
    private Long lastProcessedVersion;

    private String checksum;

    @Column(name = "last_reconciled_at")
    private OffsetDateTime lastReconciledAt;
}
