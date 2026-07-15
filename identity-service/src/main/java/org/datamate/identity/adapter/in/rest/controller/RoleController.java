package org.datamate.identity.adapter.in.rest.controller;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.RoleDto;
import org.datamate.identity.application.dto.RoleRequest;
import org.datamate.identity.application.port.in.RoleManagementUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleManagementUseCase roleManagementUseCase;

    @PostMapping
    public ResponseEntity<RoleDto> createRole(@RequestBody RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleManagementUseCase.createRole(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getRole(@PathVariable UUID id) {
        return ResponseEntity.ok(roleManagementUseCase.getRole(id));
    }

    @GetMapping
    public ResponseEntity<List<RoleDto>> listRoles() {
        return ResponseEntity.ok(roleManagementUseCase.listRoles());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleManagementUseCase.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
