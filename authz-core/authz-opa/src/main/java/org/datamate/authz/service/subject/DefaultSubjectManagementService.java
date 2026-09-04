package org.datamate.authz.service.subject;

import org.datamate.authz.api.subject.SubjectManagementService;
import org.datamate.authz.dto.subject.AuthzSubjectDto;
import org.datamate.authz.event.AuthzSubjectSyncEvent;
import org.datamate.authz.jpa.entity.AuthzSubjectJpaEntity;
import org.datamate.authz.jpa.repository.AuthzSubjectJpaRepository;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Standard N-Tier Service implementation for Subject Management.
 * Consolidates all subject-related operations for the authorization framework.
 */
@Service
public class DefaultSubjectManagementService implements SubjectManagementService {

    @EnableLogger
    private Logger log;

    private final AuthzSubjectJpaRepository subjectRepository;

    public DefaultSubjectManagementService(AuthzSubjectJpaRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Override
    @Transactional
    public void apply(AuthzSubjectSyncEvent event) {
        AuthzSubjectJpaEntity entity = findOrCreateSubject(event);

        if (isStaleEvent(entity, event)) {
            return;
        }

        updateSubjectFields(entity, event);
        handleSoftDeleteAndActivation(entity, event);

        subjectRepository.save(entity);
    }

    private AuthzSubjectJpaEntity findOrCreateSubject(AuthzSubjectSyncEvent event) {
        return subjectRepository
                .findBySubjectTypeAndSubjectId(event.subjectType(), event.subjectId())
                .orElseGet(() -> {
                    AuthzSubjectJpaEntity newEntity = new AuthzSubjectJpaEntity();
                    newEntity.setSubjectType(event.subjectType());
                    newEntity.setSubjectId(event.subjectId());
                    newEntity.setVersion(0L);
                    return newEntity;
                });
    }

    private boolean isStaleEvent(AuthzSubjectJpaEntity entity, AuthzSubjectSyncEvent event) {
        if (entity.getId() != null && event.version() <= entity.getVersion()) {
            log.debug("Skipping stale subject sync event for {} {}. Event version {}, current version {}",
                    event.subjectType(), event.subjectId(), event.version(), entity.getVersion());
            return true;
        }
        return false;
    }

    private void updateSubjectFields(AuthzSubjectJpaEntity entity, AuthzSubjectSyncEvent event) {
        entity.setSubjectName(event.subjectName());
        entity.setDisplayName(event.displayName());
        entity.setEmail(event.email());
        entity.setDescription(event.description());
        entity.setStatus(event.status());
        entity.setVersion(event.version());
        entity.setSyncedAt(LocalDateTime.now());
    }

    private void handleSoftDeleteAndActivation(AuthzSubjectJpaEntity entity, AuthzSubjectSyncEvent event) {
        if (event.deleted()) {
            if (entity.getDeletedAt() == null) {
                entity.setDeletedAt(LocalDateTime.now());
                log.info("Soft-deleted synced subject {} {}", event.subjectType(), event.subjectId());
            }
        } else {
            if (entity.getDeletedAt() != null) {
                entity.setDeletedAt(null);
                log.info("Re-activated synced subject {} {}", event.subjectType(), event.subjectId());
            } else if (entity.getId() == null) {
                log.info("Created synced subject {} {}", event.subjectType(), event.subjectId());
            } else {
                log.info("Updated synced subject {} {}", event.subjectType(), event.subjectId());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean subjectExists(SubjectType type, String subjectId) {
        return subjectRepository.findBySubjectTypeAndSubjectId(type.name(), subjectId)
                .map(entity -> entity.getDeletedAt() == null)
                .orElse(false);
    }

    private AuthzSubjectDto toDto(AuthzSubjectJpaEntity e) {
        return new AuthzSubjectDto(
                e.getSubjectId(),
                e.getSubjectName(),
                e.getDisplayName(),
                e.getEmail(),
                e.getDescription(),
                e.getStatus()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthzSubjectDto> listSubjects(SubjectType type) {
        return subjectRepository.findAllBySubjectTypeAndDeletedAtIsNull(type.name())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthzSubjectDto> findSubject(SubjectType type, String subjectId) {
        return subjectRepository
                .findBySubjectTypeAndSubjectId(type.name(), subjectId)
                .filter(e -> e.getDeletedAt() == null)
                .map(this::toDto);
    }
}
