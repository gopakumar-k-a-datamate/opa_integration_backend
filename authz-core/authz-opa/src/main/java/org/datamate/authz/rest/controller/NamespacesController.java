package org.datamate.authz.rest.controller;

import org.datamate.authz.api.endpoint.AuthorizationContext.NamespacesAuthContext;
import org.datamate.authz.api.endpoint.AuthzBeans;
import org.datamate.authz.api.endpoint.EndpointAuthorization;
import org.datamate.authz.service.policy.PolicyManagementService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Standard REST controller for retrieving available namespaces.
 * Activated by providing a bean named {@link AuthzBeans#NAMESPACES}.
 */
@RestController
@RequestMapping("/internal/authz")
@ConditionalOnBean(name = AuthzBeans.NAMESPACES)
public class NamespacesController {

    private final PolicyManagementService policyService;
    private final EndpointAuthorization authorization;

    public NamespacesController(
            PolicyManagementService policyService,
            @Qualifier(AuthzBeans.NAMESPACES) EndpointAuthorization authorization) {
        this.policyService = policyService;
        this.authorization = authorization;
    }

    @GetMapping("/namespaces")
    public List<String> getNamespaces() {
        authorization.authorize(new NamespacesAuthContext());
        return policyService.getNamespaces();
    }
}
