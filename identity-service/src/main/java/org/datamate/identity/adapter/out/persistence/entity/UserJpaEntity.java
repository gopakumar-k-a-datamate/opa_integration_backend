package org.datamate.identity.adapter.out.persistence.entity;

import com.datamate.bedrock.framework.common.auditing.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Audited
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class UserJpaEntity extends BaseAuditableEntity {
    @Id
    private UUID id;

    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "reference_system", length = 50)
    private String referenceSystem;

    @Column(name = "reference_value", length = 255)
    private String referenceValue;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "domain_version", nullable = false)
    private Long domainVersion;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
