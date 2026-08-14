package org.datamate.authz.service.policy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.api.policy.ConditionFieldRepository;
import org.datamate.authz.api.policy.PermissionRepository;
import org.datamate.authz.api.policy.PolicyCompiler;
import org.datamate.authz.api.policy.PolicyRepository;
import org.datamate.authz.api.policy.PolicyValidation;
import org.datamate.authz.api.policy.ResourceRepository;
import org.datamate.authz.dto.policy.ConditionFieldDto;
import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.exception.AuthzInvalidPayloadException;
import org.datamate.authz.exception.AuthzInvalidSyntaxException;
import org.datamate.authz.model.policy.entity.ConditionField;
import org.datamate.authz.model.policy.entity.Permission;
import org.datamate.authz.model.policy.entity.Policy;
import org.datamate.authz.model.policy.entity.Resource;
import org.datamate.authz.model.policy.enumtype.FieldType;
import org.datamate.authz.model.policy.enumtype.PolicyEffect;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.datamate.authz.model.policy.valueobject.RegoValidationResult;
import org.datamate.authz.rest.dto.PolicyItemRequest;
import org.datamate.authz.rest.dto.SavePoliciesRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultPolicyManagementServiceTest {

    @Mock private PermissionRepository permissionRepository;
    @Mock private ConditionFieldRepository conditionFieldRepository;
    @Mock private ResourceRepository resourceRepository;
    @Mock private PolicyRepository policyRepository;
    @Mock private PolicyCompiler compiler;
    @Mock private PolicyValidation validation;
    @Mock private ObjectMapper objectMapper;

    @Mock private com.datamate.bedrock.framework.common.logging.service.Logger log;

    @InjectMocks
    private DefaultPolicyManagementService service;

    @BeforeEach
    void setUp() throws Exception {
        java.lang.reflect.Field logField = DefaultPolicyManagementService.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(service, log);
    }

    @Test
    void getConditionFields_permissionNotFound() {
        when(permissionRepository.findByCode("unknown:read")).thenReturn(Optional.empty());
        List<ConditionFieldDto> result = service.getConditionFields("unknown:read");
        assertTrue(result.isEmpty());
    }

    @Test
    void getConditionFields_success() {
        Permission perm = mock(Permission.class);
        when(perm.getId()).thenReturn(10L);
        when(permissionRepository.findByCode("finance:read")).thenReturn(Optional.of(perm));

        ConditionField field = mock(ConditionField.class);
        when(field.getFieldName()).thenReturn("amount");
        when(field.getFieldType()).thenReturn(FieldType.NUMBER);

        when(conditionFieldRepository.findAllByPermissionId(10L)).thenReturn(List.of(field));

        List<ConditionFieldDto> result = service.getConditionFields("finance:read");
        assertEquals(1, result.size());
        assertEquals("amount", result.get(0).fieldName());
        assertEquals(FieldType.NUMBER, result.get(0).fieldType());
    }

    @Test
    void getNamespaces_success() {
        Resource r1 = mock(Resource.class); when(r1.getNamespace()).thenReturn("finance");
        Resource r2 = mock(Resource.class); when(r2.getNamespace()).thenReturn("hr");
        Resource r3 = mock(Resource.class); when(r3.getNamespace()).thenReturn("finance"); // duplicate

        when(resourceRepository.findAllActive()).thenReturn(List.of(r1, r2, r3));

        List<String> namespaces = service.getNamespaces();
        assertEquals(2, namespaces.size());
        assertEquals("finance", namespaces.get(0));
        assertEquals("hr", namespaces.get(1));
    }

    @Test
    void getPolicies_success() {
        Resource res = mock(Resource.class);
        when(res.getId()).thenReturn(100L);
        when(res.getNamespace()).thenReturn("finance");
        when(res.getName()).thenReturn("Finance Service");

        Permission perm = mock(Permission.class);
        when(perm.getId()).thenReturn(200L);
        when(perm.getResourceId()).thenReturn(100L);
        when(perm.getCode()).thenReturn("finance:read");
        when(perm.getAction()).thenReturn("read");

        Policy pol = mock(Policy.class);
        when(pol.getId()).thenReturn(300L);
        when(pol.getPermissionId()).thenReturn(200L);

        when(pol.getExpressionJson()).thenReturn("{\"type\": \"ConditionNode\"}");

        when(resourceRepository.findAllActive()).thenReturn(List.of(res));
        when(permissionRepository.findAllActive()).thenReturn(List.of(perm));
        when(policyRepository.findBySubject(SubjectType.ROLE, "ADMIN")).thenReturn(List.of(pol));

        JsonNode mockNode = mock(JsonNode.class);
        try {
            when(objectMapper.readTree(pol.getExpressionJson())).thenReturn(mockNode);
        } catch (Exception ignored) {}

        List<PolicyGridItemDto> result = service.getPolicies(SubjectType.ROLE, "ADMIN", "finance");
        assertEquals(1, result.size());
        PolicyGridItemDto dto = result.get(0);
        assertEquals("finance:read", dto.permissionCode());
        assertEquals("finance", dto.namespace());
        assertEquals(300L, dto.policyId());
        assertEquals(mockNode, dto.expressionJson());
    }

    @Test
    void savePolicies_emptyRequest() {
        SavePoliciesRequest req = new SavePoliciesRequest(SubjectType.ROLE, "ADMIN", "finance", List.of());

        when(policyRepository.findBySubject(SubjectType.ROLE, "ADMIN")).thenReturn(List.of());
        when(permissionRepository.findAllActive()).thenReturn(List.of());

        service.savePolicies(req);

        verify(compiler).recompile("finance");
        verify(policyRepository, never()).upsert(any(), any(), any(), any(), any(), any(), anyBoolean(), any(), anyBoolean(), any());
    }

    @Test
    void savePolicies_deleteMissingReason() {
        PolicyItemRequest item = new PolicyItemRequest("finance:delete", PolicyEffect.ALLOW, null, false, true, null, null, false, null);
        SavePoliciesRequest req = new SavePoliciesRequest(SubjectType.ROLE, "ADMIN", "finance", List.of(item));

        when(policyRepository.findBySubject(SubjectType.ROLE, "ADMIN")).thenReturn(List.of());
        when(permissionRepository.findAllActive()).thenReturn(List.of());

        assertThrows(AuthzInvalidPayloadException.class, () -> service.savePolicies(req));
    }

    @Test
    void savePolicies_disableMissingReason() {
        PolicyItemRequest item = new PolicyItemRequest("finance:delete", PolicyEffect.ALLOW, null, false, false, null, null, false, null);
        SavePoliciesRequest req = new SavePoliciesRequest(SubjectType.ROLE, "ADMIN", "finance", List.of(item));

        when(policyRepository.findBySubject(SubjectType.ROLE, "ADMIN")).thenReturn(List.of());
        when(permissionRepository.findAllActive()).thenReturn(List.of());

        assertThrows(AuthzInvalidPayloadException.class, () -> service.savePolicies(req));
    }

    @Test
    void savePolicies_softDeleteExisting() {
        PolicyItemRequest item = new PolicyItemRequest("finance:read", PolicyEffect.ALLOW, null, false, true, "Not needed", null, false, null);
        SavePoliciesRequest req = new SavePoliciesRequest(SubjectType.ROLE, "ADMIN", "finance", List.of(item));

        Permission perm = mock(Permission.class);
        when(perm.getId()).thenReturn(10L);
        when(perm.getCode()).thenReturn("finance:read");

        Policy existing = mock(Policy.class);
        when(existing.getId()).thenReturn(100L);
        when(existing.getPermissionId()).thenReturn(10L);

        when(policyRepository.findBySubject(SubjectType.ROLE, "ADMIN")).thenReturn(List.of(existing));
        when(permissionRepository.findAllActive()).thenReturn(List.of(perm));

        service.savePolicies(req);

        verify(policyRepository).softDelete(100L, "Not needed");
        verify(compiler).recompile("finance");
    }

    @Test
    void savePolicies_upsertNew_customRegoValidationFailure() {
        PolicyItemRequest item = new PolicyItemRequest("finance:read", PolicyEffect.ALLOW, null, true, false, null, null, true, "invalid rego");
        SavePoliciesRequest req = new SavePoliciesRequest(SubjectType.ROLE, "ADMIN", "finance", List.of(item));

        Permission perm = mock(Permission.class);
        when(perm.getId()).thenReturn(10L);
        when(perm.getCode()).thenReturn("finance:read");

        when(policyRepository.findBySubject(SubjectType.ROLE, "ADMIN")).thenReturn(List.of());
        when(permissionRepository.findAllActive()).thenReturn(List.of(perm));
        when(validation.validate("invalid rego")).thenReturn(new RegoValidationResult(false, List.of(new org.datamate.authz.model.policy.valueobject.RegoValidationError(1, 1, "Syntax error"))));

        assertThrows(AuthzInvalidSyntaxException.class, () -> service.savePolicies(req));
    }

    @Test
    void savePolicies_upsertNew_success() throws Exception {
        JsonNode jsonNode = mock(JsonNode.class);
        PolicyItemRequest item = new PolicyItemRequest("finance:read", PolicyEffect.ALLOW, jsonNode, true, false, null, null, false, null);
        SavePoliciesRequest req = new SavePoliciesRequest(SubjectType.ROLE, "ADMIN", "finance", List.of(item));

        Permission perm = mock(Permission.class);
        when(perm.getId()).thenReturn(10L);
        when(perm.getCode()).thenReturn("finance:read");

        when(policyRepository.findBySubject(SubjectType.ROLE, "ADMIN")).thenReturn(List.of());
        when(permissionRepository.findAllActive()).thenReturn(List.of(perm));
        when(objectMapper.writeValueAsString(jsonNode)).thenReturn("{\"some\":\"json\"}");

        service.savePolicies(req);

        verify(policyRepository).upsert(null, 10L, SubjectType.ROLE, "ADMIN", PolicyEffect.ALLOW, "{\"some\":\"json\"}", true, null, false, null);
        verify(compiler).recompile("finance");
    }

    @Test
    void savePolicies_softDeleteAbsentFromPayload() {
        SavePoliciesRequest req = new SavePoliciesRequest(SubjectType.ROLE, "ADMIN", "finance", List.of());

        Permission perm = mock(Permission.class);
        when(perm.getId()).thenReturn(10L);
        when(perm.getCode()).thenReturn("finance:read");

        Policy existing = mock(Policy.class);
        when(existing.getId()).thenReturn(100L);
        when(existing.getPermissionId()).thenReturn(10L);

        when(policyRepository.findBySubject(SubjectType.ROLE, "ADMIN")).thenReturn(List.of(existing));
        when(permissionRepository.findAllActive()).thenReturn(List.of(perm));

        service.savePolicies(req);

        verify(policyRepository).softDelete(100L, "Removed policy in state sync.");
        verify(compiler).recompile("finance");
    }
}
