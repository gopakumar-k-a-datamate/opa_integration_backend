package org.datamate.authz.jpa.service;


import org.datamate.authz.jpa.entity.PolicyJpaEntity;
import org.datamate.authz.jpa.repository.SpringDataPolicyRepository;
import org.datamate.authz.api.policy.PolicyRepository;
import org.datamate.authz.model.policy.entity.Policy;

import org.datamate.authz.model.policy.enumtype.PolicyEffect;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.datamate.authz.exception.StaleDataException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class JpaPolicyRepository implements PolicyRepository {

    public JpaPolicyRepository(SpringDataPolicyRepository repository) {
        this.repository = repository;
    }

    private final SpringDataPolicyRepository repository;

    @Override
    public List<Policy> findAllEnabled() {
        return repository.findAllByEnabledTrueAndDeletedAtIsNull()
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Policy> findAllActive() {
        return repository.findAllByDeletedAtIsNull()
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Policy> findBySubject(SubjectType subjectType, String subjectId) {
        return repository
                .findBySubjectTypeAndSubjectIdAndDeletedAtIsNull(subjectType, subjectId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Policy> findEnabledReferencingField(Long permissionId, String fieldName) {
        return repository.findEnabledReferencingField(permissionId, fieldName)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Policy upsert(Long id, Long permissionId, SubjectType subjectType, String subjectId,
                              PolicyEffect effect, String expressionJson, boolean enabled,
                              String disabledReason, boolean useCustomRego, String customRegoSnippet) {
        PolicyJpaEntity entity = repository
                .findByPermissionIdAndSubjectTypeAndSubjectIdAndDeletedAtIsNull(
                        permissionId, subjectType, subjectId)
                .orElseGet(PolicyJpaEntity::new);

        updateEntity(entity, id, permissionId, subjectType, subjectId, effect, expressionJson, enabled, disabledReason, useCustomRego, customRegoSnippet);

        try {
            return toDomain(repository.save(entity));
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new StaleDataException("The policy has been modified by another user. Please refresh and try again.");
        }
    }

    @Override
    public void updateDeprecatedStatus(Long id, boolean deprecated) {
        repository.findById(id).ifPresent(entity -> {
            entity.setDeprecated(deprecated);
            repository.save(entity);
        });
    }

    @Override
    public void softDelete(Long id, String reason) {
        repository.findById(id).ifPresent(entity -> {
            entity.setDeletedAt(LocalDateTime.now());
            entity.setDeletedReason(reason);
            repository.save(entity);
        });
    }

    @Override
    public void autoDisable(Long id, String reason) {
        repository.findById(id).ifPresent(entity -> {
            entity.setEnabled(false);
            entity.setDisabledReason(reason);
            repository.save(entity);
        });
    }

    private Policy toDomain(PolicyJpaEntity e) {
        if (e == null) return null;
        return Policy.reconstitute(
                e.getId(), e.getPermissionId(), e.getSubjectType(), e.getSubjectId(),
                e.getEffect(), e.getExpressionJson(), e.isEnabled(), e.getDisabledReason(),
                e.isDeprecated(), e.isUseCustomRego(), e.getCustomRegoSnippet(), e.getVersion(), e.getCreatedAt(), e.getUpdatedAt(), e.getDeletedAt(), e.getDeletedReason()
        );
    }

    private void updateEntity(PolicyJpaEntity entity, Long id, Long permissionId, SubjectType subjectType, String subjectId,
                             PolicyEffect effect, String expressionJson, boolean enabled, String disabledReason, boolean useCustomRego, String customRegoSnippet) {
        if (entity.getId() == null) {
            entity.setId(id);
        }
        entity.setPermissionId(permissionId);
        entity.setSubjectType(subjectType);
        entity.setSubjectId(subjectId);
        entity.setEffect(effect);
        entity.setExpressionJson(expressionJson);
        entity.setEnabled(enabled);
        entity.setDisabledReason(disabledReason);
        entity.setUseCustomRego(useCustomRego);
        entity.setCustomRegoSnippet(customRegoSnippet);
        entity.setDeletedAt(null);
    }

}
