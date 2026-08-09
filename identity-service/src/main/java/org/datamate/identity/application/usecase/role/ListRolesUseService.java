package org.datamate.identity.application.usecase.role;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.application.query.role.RoleSearchCriteria;
import org.datamate.identity.application.port.in.role.ListRolesUseCase;
import org.datamate.identity.application.port.out.role.RolePersistencePort;
import org.datamate.identity.application.mapper.role.RoleDtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListRolesUseService implements ListRolesUseCase {

    @EnableLogger
    private Logger log;

    private final RolePersistencePort rolePort;
    private final RoleDtoMapper roleDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> listRoles(RoleSearchCriteria criteria) {
        log.info("Listing roles with criteria: {}", criteria);
        List<RoleDto> roles = rolePort.searchRoles(criteria).stream()
                .map(roleDtoMapper::toDto)
                .collect(Collectors.toList());
        log.info("Successfully fetched {} roles", roles.size());
        return roles;
    }
}
