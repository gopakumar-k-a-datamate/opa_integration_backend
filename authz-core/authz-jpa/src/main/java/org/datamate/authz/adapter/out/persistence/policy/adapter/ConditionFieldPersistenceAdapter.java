package org.datamate.authz.adapter.out.persistence.policy.adapter;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.adapter.out.persistence.policy.entity.ConditionFieldJpaEntity;
import org.datamate.authz.adapter.out.persistence.policy.repository.SpringDataConditionFieldRepository;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;
import org.datamate.authz.application.port.out.policy.ConditionFieldPersistencePort;
import org.datamate.authz.domain.model.policy.entity.ConditionField;
import org.datamate.authz.adapter.out.persistence.policy.mapper.ConditionFieldPersistenceMapper;
import org.datamate.authz.adapter.out.persistence.policy.mapper.JsonMapper;
import org.datamate.authz.domain.model.policy.enumtype.FieldStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ConditionFieldPersistenceAdapter implements ConditionFieldPersistencePort {

    private final SpringDataConditionFieldRepository repository;
    private final ConditionFieldPersistenceMapper mapper;
    private final JsonMapper jsonMapper;

    @Override
    public ConditionField upsert(Long id, Long permissionId, String fieldName,
                                      FieldType fieldType, String displayName,
                                      List<String> allowedValues, String optionsEndpoint) {
        ConditionFieldJpaEntity entity = repository
                .findByPermissionIdAndFieldNameAndDeletedAtIsNull(permissionId, fieldName)
                .orElseGet(ConditionFieldJpaEntity::new);

        mapper.updateEntity(entity, id, permissionId, fieldName, fieldType, displayName, allowedValues, optionsEndpoint);

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public List<ConditionField> findActiveByPermissionId(Long permissionId) {
        return repository
                .findByPermissionIdAndStatusAndDeletedAtIsNull(permissionId, FieldStatus.ACTIVE)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ConditionField> findAllByPermissionId(Long permissionId) {
        return repository.findByPermissionIdAndDeletedAtIsNull(permissionId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<ConditionField> findByPermissionIdAndFieldName(Long permissionId,
                                                                        String fieldName) {
        return repository
                .findByPermissionIdAndFieldNameAndDeletedAtIsNull(permissionId, fieldName)
                .map(mapper::toDomain);
    }

    @Override
    public void markDeprecated(Long id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setStatus(FieldStatus.DEPRECATED);
            repository.save(entity);
        });
    }

    @Override
    public void softDelete(Long id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setDeletedAt(LocalDateTime.now());
            repository.save(entity);
        });
    }
}



