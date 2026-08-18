package org.datamate.authz.rest.controller;

import org.datamate.authz.api.endpoint.AuthorizationContext.FieldsAuthContext;
import org.datamate.authz.api.endpoint.AuthzBeans;
import org.datamate.authz.api.endpoint.EndpointAuthorization;
import org.datamate.authz.dto.policy.ConditionFieldDto;
import org.datamate.authz.service.policy.PolicyManagementService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Standard REST controller for retrieving condition fields.
 * Activated by providing a bean named {@link AuthzBeans#FIELDS}.
 */
@RestController
@RequestMapping("/internal/authz")
@ConditionalOnBean(name = AuthzBeans.FIELDS)
public class FieldsController {

    private final PolicyManagementService policyService;
    private final EndpointAuthorization authorization;

    public FieldsController(
            PolicyManagementService policyService,
            @Qualifier(AuthzBeans.FIELDS) EndpointAuthorization authorization) {
        this.policyService = policyService;
        this.authorization = authorization;
    }

    @GetMapping("/fields/{permissionCode}")
    public List<ConditionFieldDto> getFields(@PathVariable("permissionCode") String permissionCode) {
        authorization.authorize(new FieldsAuthContext(permissionCode));
        return policyService.getConditionFields(permissionCode);
    }
}
