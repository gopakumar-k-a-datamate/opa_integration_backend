package org.datamate.authz.service.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.api.policy.ConditionFieldRepository;
import org.datamate.authz.api.policy.PermissionRepository;
import org.datamate.authz.api.policy.PolicyCompiler;
import org.datamate.authz.api.policy.PolicyRepository;
import org.datamate.authz.api.policy.PolicyValidation;
import org.datamate.authz.api.policy.ResourceRepository;
import org.datamate.authz.exception.InvalidPayloadException;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.datamate.authz.rest.dto.PolicyItemRequest;
import org.datamate.authz.rest.dto.SavePoliciesRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPolicyManagementServiceTest {

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private ConditionFieldRepository conditionFieldRepository;
    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private PolicyCompiler compiler;
    @Mock
    private PolicyValidation validation;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private com.datamate.bedrock.framework.common.logging.service.Logger log;

    @InjectMocks
    private DefaultPolicyManagementService service;

    @BeforeEach
    void setUp() throws Exception {
        java.lang.reflect.Field logField = DefaultPolicyManagementService.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(service, log);
    }

    @Test
    void testSavePolicies_EmptyRequest() {
        SavePoliciesRequest request = new SavePoliciesRequest(SubjectType.ROLE, "ADMIN", "finance", List.of());
        
        when(policyRepository.findBySubject(SubjectType.ROLE, "ADMIN")).thenReturn(List.of());
        when(permissionRepository.findAllActive()).thenReturn(List.of());

        service.savePolicies(request);

        verify(compiler).recompile("finance");
        verify(policyRepository, never()).upsert(any(), any(), any(), any(), any(), any(), anyBoolean(), any(), anyBoolean(), any());
    }

    @Test
    void testSavePolicies_InvalidDeleteMissingReason() {
        PolicyItemRequest item = new PolicyItemRequest("finance:delete", null, null, false, true, null, null, false, null);
        SavePoliciesRequest request = new SavePoliciesRequest(SubjectType.ROLE, "ADMIN", "finance", List.of(item));

        when(policyRepository.findBySubject(SubjectType.ROLE, "ADMIN")).thenReturn(List.of());
        when(permissionRepository.findAllActive()).thenReturn(List.of());

        assertThrows(InvalidPayloadException.class, () -> service.savePolicies(request));
    }
}
