package org.datamate.authz.jpa.service;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.jpa.entity.ConditionFieldJpaEntity;
import org.datamate.authz.jpa.repository.SpringDataConditionFieldRepository;
import org.datamate.authz.model.policy.enumtype.FieldType;
import org.datamate.authz.api.policy.ConditionFieldRepository;
import org.datamate.authz.model.policy.entity.ConditionField;
import org.datamate.authz.jpa.mapper.ConditionFieldPersistenceMapper;
import org.datamate.authz.jpa.mapper.JsonMapper;
import org.datamate.authz.model.policy.enumtype.FieldStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JpaConditionFieldRepository implements ConditionFieldRepository {

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
    public List<ConditionField> findAllDeprecated() {
        return repository.findByStatusAndDeletedAtIsNull(FieldStatus.DEPRECATED)
                .stream().map(mapper::toDomain).toList();
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



