package org.datamate.pharmacy.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.datamate.authz.dto.policy.ConditionFieldDto;
import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.dto.policy.SavePoliciesRequest;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.datamate.pharmacy.application.service.PharmacyAuthzService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/authz")
@CrossOrigin(origins = "*") // Allows the Admin UI (Vite) to access these endpoints directly
public class AuthzPolicyController {
    
    private final PharmacyAuthzService pharmacyAuthzService;

    public AuthzPolicyController(PharmacyAuthzService pharmacyAuthzService) {
        this.pharmacyAuthzService = pharmacyAuthzService;
    }

    @GetMapping("/permissions/{permissionCode}/fields")
    public ResponseEntity<List<ConditionFieldDto>> getFields(
            @PathVariable String permissionCode) {
        return ResponseEntity.ok(pharmacyAuthzService.getFields(permissionCode));
    }

    @GetMapping("/namespaces")
    public ResponseEntity<List<String>> getNamespaces() {
        return ResponseEntity.ok(pharmacyAuthzService.getNamespaces());
    }

    @GetMapping("/policies")
    public ResponseEntity<List<PolicyGridItemDto>> getPolicies(
            @RequestParam SubjectType subjectType,
            @RequestParam String subjectId,
            @RequestParam String namespace) {
        return ResponseEntity.ok(pharmacyAuthzService.getPolicies(subjectType, subjectId, namespace));
    }

    @PutMapping("/policies")
    public ResponseEntity<Map<String, String>> savePolicies(
            @Valid @RequestBody SavePoliciesRequest request) {
            
        pharmacyAuthzService.savePolicies(request);
        
        return ResponseEntity.ok(Map.of("message",
                "Policies updated successfully. OPA bundle regenerated."));
    }
}
