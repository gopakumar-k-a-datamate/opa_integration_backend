package org.datamate.authz.application.dto.policy.mapper;

import org.datamate.authz.application.dto.policy.ConditionFieldDto;
import org.datamate.authz.domain.model.policy.entity.ConditionField;
import org.springframework.stereotype.Component;

@Component
public class ConditionFieldDtoMapper {
    public ConditionFieldDto toDto(ConditionField field) {
        if (field == null) return null;
        return new ConditionFieldDto(
                field.getFieldName(),
                field.getFieldType(),
                field.getDisplayName(),
                field.getAllowedValues(),
                field.getOptionsEndpoint(),
                field.getStatus()
        );
    }
}
