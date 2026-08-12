package org.datamate.authz.service.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.application.port.out.PermissionRepositoryPort;
import org.datamate.authz.application.port.out.PolicyCompilerPort;
import org.datamate.authz.application.port.out.PolicyRepositoryPort;
import org.datamate.authz.application.port.out.PolicyValidationPort;
import org.datamate.authz.dto.policy.PolicyItemRequest;
import org.datamate.authz.dto.policy.SavePoliciesRequest;
import org.datamate.authz.exception.InvalidPayloadException;
import org.datamate.authz.model.policy.entity.Permission;
import org.datamate.authz.model.policy.entity.Policy;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavePoliciesServiceTest {

    @Mock
    private PolicyRepositoryPort policyPort;
    @Mock
    private PermissionRepositoryPort permissionPort;
    @Mock
    private PolicyCompilerPort compilerPort;
    @Mock
    private PolicyValidationPort validationPort;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SavePoliciesService savePoliciesService;

    @Test
    void testSavePolicies_EmptyRequest() {
        SavePoliciesRequest request = new SavePoliciesRequest(SubjectType.ROLE, "ADMIN", "finance", List.of());
        
        when(policyPort.findBySubject(SubjectType.ROLE, "ADMIN")).thenReturn(List.of());
        when(permissionPort.findAllActive()).thenReturn(List.of());

        savePoliciesService.savePolicies(request);

        verify(compilerPort).recompile("finance");
        verify(policyPort, never()).upsert(any(), any(), any(), any(), any(), any(), anyBoolean(), any(), anyBoolean(), any());
    }

    @Test
    void testSavePolicies_InvalidDeleteMissingReason() {
        PolicyItemRequest item = new PolicyItemRequest("finance:delete", null, null, false, true, null, null, false, null);
        SavePoliciesRequest request = new SavePoliciesRequest(SubjectType.ROLE, "ADMIN", "finance", List.of(item));

        when(policyPort.findBySubject(SubjectType.ROLE, "ADMIN")).thenReturn(List.of());
        when(permissionPort.findAllActive()).thenReturn(List.of());

        assertThrows(InvalidPayloadException.class, () -> savePoliciesService.savePolicies(request));
    }
}
