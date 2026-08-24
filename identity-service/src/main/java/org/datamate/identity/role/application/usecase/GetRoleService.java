package org.datamate.identity.role.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.role.application.dto.RoleDto;
import org.datamate.identity.role.application.mapper.RoleDtoMapper;
import org.datamate.identity.role.application.port.in.GetRoleUseCase;
import org.datamate.identity.role.application.port.out.RolePersistencePort;
import org.datamate.identity.role.domain.exception.RoleNotFoundException;
import org.datamate.identity.role.domain.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetRoleService implements GetRoleUseCase {

    @EnableLogger
    private Logger log;

    private final RolePersistencePort rolePort;
    private final RoleDtoMapper roleDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public RoleDto getRoleById(UUID id) {
        log.info("Starting retrieval of role details for ID: {}", id);
        
        Role role = rolePort.findById(id).orElseThrow(() -> {
            log.error("Role details retrieval failed. Role not found for ID: {}", id);
            return new RoleNotFoundException();
        });

        log.info("Successfully retrieved role details for ID: {}", id);
        return roleDtoMapper.toDto(role);
    }
}


