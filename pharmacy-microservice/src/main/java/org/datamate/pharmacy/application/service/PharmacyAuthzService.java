package org.datamate.pharmacy.application.service;

import org.datamate.authz.dto.policy.ConditionFieldDto;
import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.datamate.authz.rest.dto.SavePoliciesRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PharmacyAuthzService {
    // Inject the library's consolidated service
    private final org.datamate.authz.service.policy.PolicyManagementService policyManagementService;

    public PharmacyAuthzService(
            org.datamate.authz.service.policy.PolicyManagementService policyManagementService) {
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
