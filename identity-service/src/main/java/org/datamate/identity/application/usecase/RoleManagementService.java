package org.datamate.identity.application.usecase;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.RoleDto;
import org.datamate.identity.application.dto.RoleRequest;
import org.datamate.identity.application.port.in.RoleManagementUseCase;
import org.datamate.identity.application.port.out.RolePersistencePort;
import org.datamate.identity.domain.exception.RoleAlreadyExistsException;
import org.datamate.identity.domain.exception.RoleNotFoundException;
import org.datamate.identity.domain.model.Role;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleManagementService implements RoleManagementUseCase {
    private final RolePersistencePort rolePort;

    @Override
    public RoleDto createRole(RoleRequest request) {
        if (rolePort.existsByName(request.name())) {
            throw new RoleAlreadyExistsException("Role with this name already exists");
        }
        Role role = Role.create(request.name(), request.description());
        Role saved = rolePort.save(role);
        return mapToDto(saved);
    }

    @Override
    public RoleDto getRole(UUID id) {
        Role role = rolePort.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));
        return mapToDto(role);
    }

    @Override
    public List<RoleDto> listRoles() {
        return rolePort.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRole(UUID id) {
        rolePort.delete(id);
    }

    private RoleDto mapToDto(Role role) {
        return new RoleDto(role.getId(), role.getName(), role.getDescription());
    }
}
