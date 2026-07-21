package org.datamate.authz.adapter.in.rest.controller;

import lombok.RequiredArgsConstructor;
import org.datamate.authz.application.port.in.policy.GetNamespacesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin UI — Namespaces API.
 * 
 * <p>Used by the UI to dynamically discover which namespaces (module tabs)
 * are available in the current microservice.</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/authz/namespaces")
public class NamespaceController {

    private final GetNamespacesUseCase getNamespacesUseCase;

    /**
     * Fetches all unique namespaces registered in this microservice.
     */
    @GetMapping
    public ResponseEntity<List<String>> getNamespaces() {
        return ResponseEntity.ok(getNamespacesUseCase.getNamespaces());
    }
}
