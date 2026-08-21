package org.datamate.pharmacy.domain.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleProjection {
    private UUID id;
    private String name;
    private String description;
    private Long lastProcessedVersion;
    private String checksum;
    private OffsetDateTime lastReconciledAt;
}
