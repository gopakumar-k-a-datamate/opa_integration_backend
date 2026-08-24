package org.datamate.identity.role.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.role.application.dto.RoleSelectDto;
import org.datamate.identity.role.application.mapper.RoleDtoMapper;
import org.datamate.identity.role.application.port.in.SelectRolesUseCase;
import org.datamate.identity.role.application.port.out.RolePersistencePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SelectRolesService implements SelectRolesUseCase {

    @EnableLogger
    private Logger log;

    private final RolePersistencePort rolePort;
    private final RoleDtoMapper roleDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleSelectDto> selectRoles(String search) {
        log.info("Selecting active roles with search query: '{}'", search);
        return rolePort.findActiveRoles(search)
                .stream()
                .map(roleDtoMapper::toSelectDto)
                .collect(Collectors.toList());
    }
}


