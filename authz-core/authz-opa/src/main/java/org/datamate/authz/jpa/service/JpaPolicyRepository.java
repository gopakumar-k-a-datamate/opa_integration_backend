package org.datamate.authz.jpa.service;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.jpa.entity.PolicyJpaEntity;
import org.datamate.authz.jpa.repository.SpringDataPolicyRepository;
import org.datamate.authz.api.policy.PolicyRepository;
import org.datamate.authz.model.policy.entity.Policy;
import org.datamate.authz.jpa.mapper.PolicyPersistenceMapper;
import org.datamate.authz.model.policy.enumtype.PolicyEffect;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.datamate.authz.exception.StaleDataException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JpaPolicyRepository implements PolicyRepository {

    private final SpringDataPolicyRepository repository;
    private final PolicyPersistenceMapper mapper;

    @Override
    public List<Policy> findAllEnabled() {
        return repository.findAllByEnabledTrueAndDeletedAtIsNull()
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Policy> findAllActive() {
        return repository.findAllByDeletedAtIsNull()
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Policy> findBySubject(SubjectType subjectType, String subjectId) {
        return repository
                .findBySubjectTypeAndSubjectIdAndDeletedAtIsNull(subjectType, subjectId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Policy> findEnabledReferencingField(Long permissionId, String fieldName) {
        return repository.findEnabledReferencingField(permissionId, fieldName)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Policy upsert(Long id, Long permissionId, SubjectType subjectType, String subjectId,
                              PolicyEffect effect, String expressionJson, boolean enabled,
                              String disabledReason) {
        PolicyJpaEntity entity = repository
                .findByPermissionIdAndSubjectTypeAndSubjectIdAndDeletedAtIsNull(
                        permissionId, subjectType, subjectId)
                .orElseGet(PolicyJpaEntity::new);

        mapper.updateEntity(entity, id, permissionId, subjectType, subjectId, effect, expressionJson, enabled, disabledReason);

        try {
            return mapper.toDomain(repository.save(entity));
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
}




