package org.datamate.authz.rest.controller;

import org.datamate.authz.api.endpoint.AuthorizationContext.PoliciesAuthContext;
import org.datamate.authz.api.endpoint.AuthzBeans;
import org.datamate.authz.api.endpoint.EndpointAuthorization;
import org.datamate.authz.dto.policy.PolicyGridItemDto;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.datamate.authz.service.policy.PolicyManagementService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Standard REST controller for retrieving policies.
 * Activated by providing a bean named {@link AuthzBeans#POLICIES}.
 */
@RestController
@RequestMapping("/internal/authz")
@ConditionalOnBean(name = AuthzBeans.POLICIES)
public class PoliciesController {

    private final PolicyManagementService policyService;
    private final EndpointAuthorization authorization;

    public PoliciesController(
            PolicyManagementService policyService,
            @Qualifier(AuthzBeans.POLICIES) EndpointAuthorization authorization) {
        this.policyService = policyService;
        this.authorization = authorization;
    }

    @GetMapping("/policies")
    public List<PolicyGridItemDto> getPolicies(
            @RequestParam("subjectType") SubjectType subjectType,
            @RequestParam("subjectId") String subjectId,
            @RequestParam("namespace") String namespace) {

        authorization.authorize(new PoliciesAuthContext(subjectType, subjectId, namespace));
        return policyService.getPolicies(subjectType, subjectId, namespace);
    }
}
