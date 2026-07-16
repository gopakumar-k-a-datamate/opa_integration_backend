package org.datamate.identity.adapter.in.rest.controller;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.RoleDto;
import org.datamate.identity.application.dto.RoleRequest;
import org.datamate.identity.application.port.in.RoleManagementUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<RoleDto> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleManagementUseCase.getRole(id));
    }

    @GetMapping
    public ResponseEntity<List<RoleDto>> listRoles() {
        return ResponseEntity.ok(roleManagementUseCase.listRoles());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleManagementUseCase.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
