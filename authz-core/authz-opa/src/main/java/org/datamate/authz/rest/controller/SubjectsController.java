package org.datamate.authz.rest.dto;

import org.datamate.authz.dto.subject.AuthzSubjectDto;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.datamate.authz.api.subject.SubjectManagementService;
import org.datamate.authz.api.endpoint.AuthzBeans;
import org.datamate.authz.api.endpoint.EndpointAuthorization;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@RestController
@RequestMapping("/internal/authz/subjects")
public class SubjectsController {

    private final SubjectManagementService subjectManagementService;
    private final EndpointAuthorization authorization;

    public SubjectsController(SubjectManagementService subjectManagementService, 
                              @Qualifier(AuthzBeans.SUBJECTS) EndpointAuthorization authorization) {
        this.subjectManagementService = subjectManagementService;
        this.authorization = authorization;
    }

    @GetMapping
    public ResponseEntity<List<AuthzSubjectDto>> listSubjects(
            @RequestParam("type") SubjectType type) {
        
        authorization.authorize(new org.datamate.authz.api.endpoint.AuthorizationContext.SubjectsAuthContext(type));
        return ResponseEntity.ok(subjectManagementService.listSubjects(type));
    }
}
