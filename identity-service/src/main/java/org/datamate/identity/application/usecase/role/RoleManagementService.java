package org.datamate.identity.application.usecase.role;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.dto.role.RoleRequest;
import org.datamate.identity.application.port.in.role.RoleManagementUseCase;
import org.datamate.identity.application.port.out.role.RolePersistencePort;
import org.datamate.identity.domain.exception.role.RoleAlreadyExistsException;
import org.datamate.identity.domain.exception.role.RoleNotFoundException;
import org.datamate.identity.domain.model.Role;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleManagementService implements RoleManagementUseCase {

    @EnableLogger
    private Logger log;

    private final RolePersistencePort rolePort;

    @Override
    public RoleDto createRole(RoleRequest request) {
        log.info("Creating role '{}'", request.name());
        if (rolePort.existsByName(request.name())) {
            log.warn("Role creation failed: role '{}' already exists", request.name());
            throw new RoleAlreadyExistsException("Role with this name already exists");
        }
        Role role = Role.create(request.name(), request.description());
        Role saved = rolePort.save(role);
        log.info("Role '{}' created with id {}", saved.getName(), saved.getId());
        return mapToDto(saved);
    }

    @Override
    public RoleDto getRole(Long id) {
        log.info("Fetching role with id {}", id);
        Role role = rolePort.findById(id)
                .orElseThrow(() -> {
                    log.warn("Role not found with id {}", id);
                    return new RoleNotFoundException("Role not found");
                });
        return mapToDto(role);
    }

    @Override
    public List<RoleDto> listRoles() {
        List<RoleDto> roles = rolePort.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        log.info("Listed {} roles", roles.size());
        return roles;
    }

    @Override
    public void deleteRole(Long id) {
        log.info("Deleting role with id {}", id);
        rolePort.delete(id);
    }

    private RoleDto mapToDto(Role role) {
        return new RoleDto(role.getId(), role.getName(), role.getDescription());
    }
}
