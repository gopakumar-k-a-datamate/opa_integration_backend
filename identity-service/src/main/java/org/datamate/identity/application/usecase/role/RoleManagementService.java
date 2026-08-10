package org.datamate.identity.application.usecase.role;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.dto.role.RoleRequest;
import org.datamate.identity.application.port.in.role.RoleManagementUseCase;
import org.datamate.identity.application.port.out.role.RolePersistencePort;
import org.datamate.identity.application.port.out.SecurityContextPort;
import org.datamate.identity.domain.exception.role.RoleAlreadyExistsException;
import org.datamate.identity.domain.exception.role.RoleNotFoundException;
import org.datamate.identity.domain.model.Role;
import org.datamate.identity.application.mapper.role.RoleDtoMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleManagementService implements RoleManagementUseCase {

    @EnableLogger
    private Logger log;

    private final RolePersistencePort rolePort;
    private final SecurityContextPort securityContextPort;
    private final RoleDtoMapper roleDtoMapper;

    @Override
    public RoleDto getRole(UUID id) {
        log.info("Fetching role with id {}", id);
        Role role = rolePort.findById(id)
                .orElseThrow(() -> {
                    log.warn("Role not found with id {}", id);
                    return new RoleNotFoundException();
                });
        return roleDtoMapper.toDto(role);
    }

    @Override
    public void deleteRole(UUID id) {
        log.info("Deleting role with id {}", id);
        rolePort.delete(id);
    }
}
