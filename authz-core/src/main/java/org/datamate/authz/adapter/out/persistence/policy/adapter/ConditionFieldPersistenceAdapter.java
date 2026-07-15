package org.datamate.authz.adapter.out.persistence.policy.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.adapter.out.persistence.policy.entity.ConditionFieldJpaEntity;
import org.datamate.authz.adapter.out.persistence.policy.repository.SpringDataConditionFieldRepository;
import org.datamate.authz.domain.model.policy.enumtype.FieldType;
import org.datamate.authz.application.port.out.policy.ConditionFieldPersistencePort;
import org.datamate.authz.domain.model.policy.entity.ConditionField;
import org.datamate.authz.domain.model.policy.enumtype.FieldStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConditionFieldPersistenceAdapter implements ConditionFieldPersistencePort {

    private final SpringDataConditionFieldRepository repository;
    private final ObjectMapper objectMapper;

    public ConditionFieldPersistenceAdapter(SpringDataConditionFieldRepository repository,
                                                 ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ConditionField upsert(UUID id, UUID permissionId, String fieldName,
                                      FieldType fieldType, String displayName,
                                      List<String> allowedValues, String optionsEndpoint) {
        ConditionFieldJpaEntity entity = repository
                .findByPermissionIdAndFieldNameAndDeletedAtIsNull(permissionId, fieldName)
                .orElseGet(ConditionFieldJpaEntity::new);

        if (entity.getId() == null) {
            entity.setId(id);
        }
        entity.setPermissionId(permissionId);
        entity.setFieldName(fieldName);
        entity.setFieldType(fieldType);
        entity.setDisplayName(displayName);
        entity.setAllowedValues(serializeList(allowedValues));
        entity.setOptionsEndpoint(optionsEndpoint);
        entity.setStatus(FieldStatus.ACTIVE);
        entity.setDeletedAt(null);

        return toDomain(repository.save(entity));
    }

    @Override
    public List<ConditionField> findActiveByPermissionId(UUID permissionId) {
        return repository
                .findByPermissionIdAndStatusAndDeletedAtIsNull(permissionId, FieldStatus.ACTIVE)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<ConditionField> findAllByPermissionId(UUID permissionId) {
        return repository.findByPermissionIdAndDeletedAtIsNull(permissionId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<ConditionField> findByPermissionIdAndFieldName(UUID permissionId,
                                                                        String fieldName) {
        return repository
                .findByPermissionIdAndFieldNameAndDeletedAtIsNull(permissionId, fieldName)
                .map(this::toDomain);
    }

    @Override
    public void markDeprecated(UUID id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setStatus(FieldStatus.DEPRECATED);
            repository.save(entity);
        });
    }

    @Override
    public void softDelete(UUID id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setDeletedAt(LocalDateTime.now());
            repository.save(entity);
        });
    }

    private ConditionField toDomain(ConditionFieldJpaEntity e) {
        return new ConditionField(
                e.getId(), e.getPermissionId(), e.getFieldName(), e.getFieldType(),
                e.getDisplayName(), deserializeList(e.getAllowedValues()),
                e.getOptionsEndpoint(), e.getStatus(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getDeletedAt()
        );
    }

    private String serializeList(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> deserializeList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}

