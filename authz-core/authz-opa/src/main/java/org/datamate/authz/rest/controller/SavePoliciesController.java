package org.datamate.authz.rest.controller;

import org.datamate.authz.api.endpoint.AuthorizationContext.SavePoliciesAuthContext;
import org.datamate.authz.api.endpoint.AuthzBeans;
import org.datamate.authz.api.endpoint.EndpointAuthorization;
import org.datamate.authz.rest.dto.SavePoliciesRequest;
import org.datamate.authz.service.policy.PolicyManagementService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Standard REST controller for saving policies.
 * Activated by providing a bean named {@link AuthzBeans#SAVE_POLICIES}.
 */
@RestController
@RequestMapping("/internal/authz")
@ConditionalOnBean(name = AuthzBeans.SAVE_POLICIES)
public class SavePoliciesController {

    private final PolicyManagementService policyService;
    private final EndpointAuthorization authorization;

    public SavePoliciesController(
            PolicyManagementService policyService,
            @Qualifier(AuthzBeans.SAVE_POLICIES) EndpointAuthorization authorization) {
        this.policyService = policyService;
        this.authorization = authorization;
    }

    @PutMapping("/policies")
    public ResponseEntity<Void> savePolicies(@RequestBody SavePoliciesRequest request) {
        authorization.authorize(new SavePoliciesAuthContext(request));
        policyService.savePolicies(request);
        return ResponseEntity.ok().build();
    }
}
