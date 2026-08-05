package org.datamate.authz.jpa.service;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.jpa.entity.ConditionFieldJpaEntity;
import org.datamate.authz.jpa.repository.SpringDataConditionFieldRepository;
import org.datamate.authz.model.policy.enumtype.FieldType;
import org.datamate.authz.api.policy.ConditionFieldRepository;
import org.datamate.authz.model.policy.entity.ConditionField;

import org.datamate.authz.model.policy.enumtype.FieldStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JpaConditionFieldRepository implements ConditionFieldRepository {

    private final SpringDataConditionFieldRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public ConditionField upsert(Long id, Long permissionId, String fieldName,
                                      FieldType fieldType, String displayName,
                                      List<String> allowedValues, String optionsEndpoint) {
        ConditionFieldJpaEntity entity = repository
                .findByPermissionIdAndFieldNameAndDeletedAtIsNull(permissionId, fieldName)
                .orElseGet(ConditionFieldJpaEntity::new);

        updateEntity(entity, id, permissionId, fieldName, fieldType, displayName, allowedValues, optionsEndpoint);

        return toDomain(repository.save(entity));
    }

    @Override
    public List<ConditionField> findActiveByPermissionId(Long permissionId) {
        return repository
                .findByPermissionIdAndStatusAndDeletedAtIsNull(permissionId, FieldStatus.ACTIVE)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<ConditionField> findAllByPermissionId(Long permissionId) {
        return repository.findByPermissionIdAndDeletedAtIsNull(permissionId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<ConditionField> findByPermissionIdAndFieldName(Long permissionId,
                                                                        String fieldName) {
        return repository
                .findByPermissionIdAndFieldNameAndDeletedAtIsNull(permissionId, fieldName)
                .map(this::toDomain);
    }

    @Override
    public List<ConditionField> findAllDeprecated() {
        return repository.findByStatusAndDeletedAtIsNull(FieldStatus.DEPRECATED)
                .stream().map(this::toDomain).toList();
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

    private ConditionField toDomain(ConditionFieldJpaEntity e) {
        if (e == null) return null;
        return ConditionField.reconstitute(
                e.getId(), e.getPermissionId(), e.getFieldName(), e.getFieldType(),
                e.getDisplayName(), deserializeList(e.getAllowedValues()),
                e.getOptionsEndpoint(), e.getStatus(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getDeletedAt()
        );
    }

    private void updateEntity(ConditionFieldJpaEntity entity, Long id, Long permissionId, String fieldName,
                             FieldType fieldType, String displayName, List<String> allowedValues, String optionsEndpoint) {
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
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}



