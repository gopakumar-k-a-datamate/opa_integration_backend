package org.datamate.pharmacy.application.service;

import lombok.RequiredArgsConstructor;
import org.datamate.authz.application.port.in.GetConditionFieldsUseCase;
import org.datamate.authz.application.port.in.GetNamespacesUseCase;
import org.datamate.authz.application.port.in.GetPoliciesUseCase;
import org.datamate.authz.application.port.in.SavePoliciesUseCase;
import org.datamate.authz.dto.policy.ConditionFieldDto;
import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.dto.policy.SavePoliciesRequest;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PharmacyAuthzService {
    
    // Inject the library's inbound ports
    private final GetConditionFieldsUseCase getConditionFieldsUseCase;
    private final GetNamespacesUseCase getNamespacesUseCase;
    private final GetPoliciesUseCase getPoliciesUseCase;
    private final SavePoliciesUseCase savePoliciesUseCase;

    public PharmacyAuthzService(
            GetConditionFieldsUseCase getConditionFieldsUseCase,
            GetNamespacesUseCase getNamespacesUseCase,
            GetPoliciesUseCase getPoliciesUseCase,
            SavePoliciesUseCase savePoliciesUseCase) {
        this.getConditionFieldsUseCase = getConditionFieldsUseCase;
        this.getNamespacesUseCase = getNamespacesUseCase;
        this.getPoliciesUseCase = getPoliciesUseCase;
        this.savePoliciesUseCase = savePoliciesUseCase;
    }

    public List<ConditionFieldDto> getFields(String permissionCode) {
        // custom validations could be added here
        return getConditionFieldsUseCase.getFields(permissionCode);
    }

    public List<String> getNamespaces() {
        // custom validations could be added here
        return getNamespacesUseCase.getNamespaces();
    }

    public List<PolicyGridItemDto> getPolicies(SubjectType subjectType, String subjectId, String namespace) {
        // custom validations could be added here
        return getPoliciesUseCase.getPolicies(subjectType, subjectId, namespace);
    }

    public void savePolicies(SavePoliciesRequest request) {
        // custom validations could be added here
        // e.g., verifying if the subject exists in the Pharmacy database
        
        savePoliciesUseCase.savePolicies(request);
    }
}
