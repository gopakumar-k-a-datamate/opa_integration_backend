package org.datamate.authz.rest.controller;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.dto.policy.SavePoliciesRequest;
import org.datamate.authz.service.policy.GetPoliciesService;
import org.datamate.authz.service.policy.SavePoliciesService;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin UI — Role-Permission Grid API.
 *
 * <p>All endpoints are prefixed {@code /internal/authz/} and intended for internal
 * Admin UI access (should be secured via API Gateway or network policy).</p>
 *
 * <ul>
 *   <li>{@code GET  /internal/authz/policies} — fetch permission grid for a subject</li>
 *   <li>{@code PUT  /internal/authz/policies} — full-state sync of policies for a subject</li>
 * </ul>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/authz/policies")
public class PolicyController {

    private final GetPoliciesService GetPoliciesService;
    private final SavePoliciesService SavePoliciesService;
    /**
     * Fetches the complete permission matrix for a subject (Role or User).
     *
     * @param subjectType {@code ROLE} or {@code USER}
     * @param subjectId   role name (e.g. {@code ACCOUNTANT}) or user ID (e.g. {@code 42})
     */
    @GetMapping
    public ResponseEntity<List<PolicyGridItemDto>> getPolicies(
            @RequestParam SubjectType subjectType,
            @RequestParam String subjectId,
            @RequestParam String namespace) {
        return ResponseEntity.ok(GetPoliciesService.getPolicies(subjectType, subjectId, namespace));
    }

    /**
     * Full-state sync — saves all policy changes for the given subject.
     * Triggers OPA bundle recompilation on success.
     */
    @PutMapping
    public ResponseEntity<Map<String, String>> savePolicies(
            @RequestBody SavePoliciesRequest request) {
        SavePoliciesService.savePolicies(request);
        return ResponseEntity.ok(Map.of("message",
                "Policies updated successfully. OPA bundle regenerated."));
    }
}





