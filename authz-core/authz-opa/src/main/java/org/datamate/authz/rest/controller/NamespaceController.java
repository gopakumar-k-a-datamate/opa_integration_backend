package org.datamate.authz.rest.controller;

import org.datamate.authz.service.policy.GetNamespacesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

/**
 * Admin UI — Namespaces API.
 * 
 * <p>Used by the UI to dynamically discover which namespaces (module tabs)
 * are available in the current microservice.</p>
 */
@RestController
@ConditionalOnProperty(name = "datamate.authz.admin.enabled", havingValue = "true")
@RequestMapping("/internal/authz/namespaces")
public class NamespaceController {

    private final GetNamespacesService GetNamespacesService;

    /**
     * Fetches all unique namespaces registered in this microservice.
     */
    @GetMapping
    public ResponseEntity<List<String>> getNamespaces() {
        return ResponseEntity.ok(GetNamespacesService.getNamespaces());
    }

    public NamespaceController(GetNamespacesService GetNamespacesService) {
        this.GetNamespacesService = GetNamespacesService;
    }

}
