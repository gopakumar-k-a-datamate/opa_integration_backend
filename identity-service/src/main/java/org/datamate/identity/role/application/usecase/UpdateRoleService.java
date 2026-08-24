package org.datamate.identity.role.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.role.application.dto.RoleDto;
import org.datamate.identity.role.application.dto.UpdateRoleRequest;
import org.datamate.identity.role.application.mapper.RoleDtoMapper;
import org.datamate.identity.role.application.port.in.UpdateRoleUseCase;
import org.datamate.identity.role.application.port.out.RolePersistencePort;
import org.datamate.identity.role.domain.exception.RoleAlreadyExistsException;
import org.datamate.identity.role.domain.exception.RoleNotFoundException;
import org.datamate.identity.role.domain.model.Role;
import com.datamate.bedrock.framework.common.ddd.datatype.EntityReference;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateRoleService implements UpdateRoleUseCase {

    @EnableLogger
    private Logger log;

    private final RolePersistencePort rolePort;
    private final RoleDtoMapper roleDtoMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public RoleDto updateRole(UUID id, UpdateRoleRequest request, EntityReference<UUID> adminUserRef) {
        log.info("Starting update of role ID: {} by admin: {}", id, adminUserRef);

        Role role = rolePort.findById(id).orElseThrow(() -> {
            log.error("Role update failed. Role not found for ID: {}", id);
            return new RoleNotFoundException();
        });

        // Uniqueness validation (ensure the new name is not already taken by another role)
        if (rolePort.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            log.error("Role update failed. Role name '{}' is already taken by another role.", request.name());
            throw new RoleAlreadyExistsException();
        }

        // Domain-driven validation and mutation
        Role updatedRole = role.updateInformation(
                request.name(),
                request.description(),
                adminUserRef
        );

        Role savedRole = rolePort.save(updatedRole);

        // Publish events registered in the domain aggregate
        updatedRole.pullEvents().forEach(eventPublisher::publishEvent);

        log.info("Successfully updated role ID: {} by admin: {}", id, adminUserRef);
        return roleDtoMapper.toDto(savedRole);
    }
}


