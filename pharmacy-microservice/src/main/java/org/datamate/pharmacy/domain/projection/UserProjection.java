package org.datamate.pharmacy.domain.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProjection {
    private UUID id;
    private String username;
    private String email;
    private String status;
    private Set<RoleProjection> roles = new HashSet<>();
    private Long lastProcessedVersion;
    private String checksum;
    private OffsetDateTime lastReconciledAt;
}
