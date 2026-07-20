package org.datamate.authz.application.dto.policy.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.datamate.authz.application.dto.policy.PolicyGridItemDto;
import org.datamate.authz.domain.model.policy.entity.Permission;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.domain.model.policy.entity.Resource;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PolicyDtoMapper {

    private final ObjectMapper objectMapper;

    public PolicyGridItemDto toDto(Permission permission, Resource resource, Policy policy) {
        if (policy == null) {
            return new PolicyGridItemDto(
                    permission.getCode(),
                    permission.getAction(),
                    resource.getNamespace(),
                    resource.getName(),
                    null, null, null, false, null, null
            );
        }

        JsonNode expressionNode = parseJson(policy.getExpressionJson());
        return new PolicyGridItemDto(
                permission.getCode(),
                permission.getAction(),
                resource.getNamespace(),
                resource.getName(),
                policy.getId(),
                policy.getEffect(),
                expressionNode,
                policy.isEnabled(),
                policy.getDisabledReason(),
                policy.getDeletedReason()
        );
    }

    private JsonNode parseJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }
}
