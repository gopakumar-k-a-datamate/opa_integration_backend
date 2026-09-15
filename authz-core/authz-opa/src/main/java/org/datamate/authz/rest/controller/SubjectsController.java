package org.datamate.authz.rest.controller;

import com.datamate.bedrock.framework.common.pagination.PageQuery;
import com.datamate.bedrock.framework.common.pagination.Paged;
import org.datamate.authz.api.endpoint.AuthzBeans;
import org.datamate.authz.api.endpoint.AuthorizationContext;
import org.datamate.authz.api.endpoint.EndpointAuthorization;
import org.datamate.authz.api.subject.SubjectManagementService;
import org.datamate.authz.dto.subject.AuthzSubjectDto;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Standard REST controller for listing, searching, and paginating subjects (Roles and Users).
 * Activated by providing a bean named {@link AuthzBeans#SUBJECTS}.
 */
@RestController
@RequestMapping("/internal/authz/subjects")
@ConditionalOnBean(name = AuthzBeans.SUBJECTS)
public class SubjectsController {

    private final SubjectManagementService subjectManagementService;
    private final EndpointAuthorization authorization;

    public SubjectsController(
            SubjectManagementService subjectManagementService,
            @Qualifier(AuthzBeans.SUBJECTS) EndpointAuthorization authorization) {
        this.subjectManagementService = subjectManagementService;
        this.authorization = authorization;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Paged<AuthzSubjectDto> getSubjects(
            @RequestParam(value = "type", required = false) SubjectType type,
            @RequestParam(value = "subjectType", required = false) SubjectType subjectType,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        SubjectType targetType = (type != null) ? type : subjectType;
        if (targetType != null) {
            authorization.authorize(new AuthorizationContext.SubjectsAuthContext(targetType));
        }

        PageQuery pageQuery = new PageQuery(page, size);
        return subjectManagementService.getSubjects(targetType, search, pageQuery);
    }
}
