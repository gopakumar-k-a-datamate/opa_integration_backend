package org.datamate.authz.rest.controller;

import org.datamate.authz.api.endpoint.AuthorizationContext.PoliciesAuthContext;
import org.datamate.authz.api.endpoint.AuthzBeans;
import org.datamate.authz.api.endpoint.EndpointAuthorization;
import org.datamate.authz.dto.policy.PolicySearchQuery;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.datamate.authz.service.policy.PolicyManagementService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.datamate.bedrock.framework.common.pagination.PageQuery;

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
    @ResponseStatus(HttpStatus.OK)
    public Object getPolicies(
            @RequestParam("subjectType") SubjectType subjectType,
            @RequestParam("subjectId") String subjectId,
            @RequestParam("namespace") String namespace,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {

        authorization.authorize(new PoliciesAuthContext(subjectType, subjectId, namespace));
        PolicySearchQuery policySearchQuery = new PolicySearchQuery(subjectType, subjectId, namespace, search);

        if (page != null || size != null) {
            int pageNum = (page != null) ? page : 1;
            int pageSize = (size != null) ? size : 10;
            PageQuery pageQuery = new PageQuery(pageNum, pageSize);
            return policyService.getPolicies(policySearchQuery, pageQuery);
        }

        return policyService.getPolicies(policySearchQuery);
    }
}
