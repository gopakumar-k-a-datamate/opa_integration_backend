package org.datamate.identity.adapter.in.rest.controller;

import org.datamate.identity.application.dto.authorization.PermissionNamespaceDto;
import org.datamate.identity.application.dto.authorization.ResourceDto;
import org.datamate.identity.application.dto.authorization.PermissionDto;
import org.datamate.identity.application.port.in.authorization.ListPermissionNamespacesUseCase;
import org.datamate.identity.application.port.in.authorization.ListResourcesByNamespaceUseCase;
import org.datamate.identity.application.port.in.authorization.ListPermissionsByResourceUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authorization")
public class AuthorizationCatalogController {

    private final ListPermissionNamespacesUseCase listNamespacesUseCase;
    private final ListResourcesByNamespaceUseCase listResourcesUseCase;
    private final ListPermissionsByResourceUseCase listPermissionsUseCase;

    public AuthorizationCatalogController(ListPermissionNamespacesUseCase listNamespacesUseCase, ListResourcesByNamespaceUseCase listResourcesUseCase, ListPermissionsByResourceUseCase listPermissionsUseCase) {
        this.listNamespacesUseCase = listNamespacesUseCase;
        this.listResourcesUseCase = listResourcesUseCase;
        this.listPermissionsUseCase = listPermissionsUseCase;
    }


    @GetMapping("/namespaces")
    public ResponseEntity<List<PermissionNamespaceDto>> getNamespaces() {
        return ResponseEntity.ok(listNamespacesUseCase.listNamespaces());
    }

    @GetMapping("/namespaces/{namespaceId}/resources")
    public ResponseEntity<List<ResourceDto>> getResources(@PathVariable UUID namespaceId) {
        return ResponseEntity.ok(listResourcesUseCase.listResources(namespaceId));
    }

    @GetMapping("/resources/{resourceId}/permissions")
    public ResponseEntity<List<PermissionDto>> getPermissions(@PathVariable UUID resourceId) {
        return ResponseEntity.ok(listPermissionsUseCase.listPermissions(resourceId));
    }
}
