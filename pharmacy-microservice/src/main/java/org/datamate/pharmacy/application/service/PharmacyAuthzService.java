package org.datamate.pharmacy.application.service;

import lombok.RequiredArgsConstructor;
import org.datamate.authz.service.policy.PolicyManagementService;
import org.datamate.authz.dto.policy.ConditionFieldDto;
import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.rest.dto.SavePoliciesRequest;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PharmacyAuthzService {
    
    private final PolicyManagementService policyManagementService;

    public PharmacyAuthzService(PolicyManagementService policyManagementService) {
        this.policyManagementService = policyManagementService;
    }

    public List<ConditionFieldDto> getFields(String permissionCode) {
        // custom validations could be added here
        return policyManagementService.getConditionFields(permissionCode);
    }

    public List<String> getNamespaces() {
        // custom validations could be added here
        return policyManagementService.getNamespaces();
    }

    public List<PolicyGridItemDto> getPolicies(SubjectType subjectType, String subjectId, String namespace) {
        // custom validations could be added here
        return policyManagementService.getPolicies(subjectType, subjectId, namespace);
    }

    public void savePolicies(SavePoliciesRequest request) {
        // custom validations could be added here
        // e.g., verifying if the subject exists in the Pharmacy database
        
        policyManagementService.savePolicies(request);
    }
}
