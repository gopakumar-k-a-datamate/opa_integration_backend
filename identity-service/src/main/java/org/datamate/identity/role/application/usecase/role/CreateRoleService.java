package org.datamate.identity.role.application.usecase.role;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.role.application.dto.role.RoleDto;
import org.datamate.identity.role.application.dto.role.RoleRequest;
import org.datamate.identity.role.application.port.in.role.CreateRoleUseCase;
import org.datamate.identity.auth.application.port.out.auth.SecurityContextPort;
import org.datamate.identity.role.application.port.out.role.RolePersistencePort;
import org.datamate.identity.role.domain.exception.role.RoleAlreadyExistsException;
import org.datamate.identity.role.domain.model.role.entity.Role;
import org.datamate.identity.role.application.mapper.role.RoleDtoMapper;
import org.datamate.identity.role.application.service.role.AuditActorResolver;
import com.datamate.bedrock.framework.common.ddd.datatype.EntityReference;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateRoleService implements CreateRoleUseCase {

    @EnableLogger
    private Logger log;

    private final RolePersistencePort rolePort;
    private final SecurityContextPort securityContextPort;
    private final RoleDtoMapper roleDtoMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditActorResolver auditActorResolver;

    @Override
    public RoleDto createRole(RoleRequest request) {
        log.info("Creating role '{}'", request.name());
        if (rolePort.existsByNameIgnoreCase(request.name())) {
            log.warn("Role creation failed: role '{}' already exists", request.name());
            throw new RoleAlreadyExistsException();
        }
        String currentUser = securityContextPort.getCurrentUsername();
        EntityReference<UUID> createdBy = auditActorResolver.resolve(currentUser);
        Role role = Role.create(request.name(), request.description(), createdBy);
        Role saved = rolePort.save(role);
        
        // Register the event and publish it
        saved.publishCreationEvent().pullEvents().forEach(eventPublisher::publishEvent);
        
        log.info("Role '{}' created with id {}", saved.getName(), saved.getId());
        return roleDtoMapper.toDto(saved);
    }
}
