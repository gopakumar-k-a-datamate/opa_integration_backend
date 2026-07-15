package org.datamate.authz.adapter.out.persistence.policy.adapter;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.adapter.out.persistence.policy.entity.PolicyJpaEntity;
import org.datamate.authz.adapter.out.persistence.policy.repository.SpringDataPolicyRepository;
import org.datamate.authz.application.port.out.policy.PolicyPersistencePort;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.adapter.out.persistence.policy.mapper.PolicyPersistenceMapper;
import org.datamate.authz.domain.model.policy.enumtype.PolicyEffect;
import org.datamate.authz.domain.model.policy.enumtype.SubjectType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class PolicyPersistenceAdapter implements PolicyPersistencePort {

    private final SpringDataPolicyRepository repository;
    private final PolicyPersistenceMapper mapper;
@Override
    public List<Policy> findAllEnabled() {
        return repository.findAllByEnabledTrueAndDeletedAtIsNull()
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Policy> findBySubject(SubjectType subjectType, String subjectId) {
        return repository
                .findBySubjectTypeAndSubjectIdAndDeletedAtIsNull(subjectType, subjectId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Policy> findByPermissionIdAndSubject(UUID permissionId,
                                                              SubjectType subjectType,
                                                              String subjectId) {
        return repository
                .findByPermissionIdAndSubjectTypeAndSubjectIdAndDeletedAtIsNull(
                        permissionId, subjectType, subjectId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Policy> findEnabledReferencingField(UUID permissionId, String fieldName) {
        return repository.findEnabledReferencingField(permissionId, fieldName)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Policy upsert(UUID id, UUID permissionId, SubjectType subjectType, String subjectId,
                              PolicyEffect effect, String expressionJson, boolean enabled,
                              String disabledReason) {
        PolicyJpaEntity entity = repository
                .findByPermissionIdAndSubjectTypeAndSubjectIdAndDeletedAtIsNull(
                        permissionId, subjectType, subjectId)
                .orElseGet(PolicyJpaEntity::new);

        mapper.updateEntity(entity, id, permissionId, subjectType, subjectId, effect, expressionJson, enabled, disabledReason);

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public void softDelete(UUID id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setDeletedAt(LocalDateTime.now());
            repository.save(entity);
        });
    }

    @Override
    public void autoDisable(UUID id, String reason) {
        repository.findById(id).ifPresent(entity -> {
            entity.setEnabled(false);
            entity.setDisabledReason(reason);
            repository.save(entity);
        });
    }
}




