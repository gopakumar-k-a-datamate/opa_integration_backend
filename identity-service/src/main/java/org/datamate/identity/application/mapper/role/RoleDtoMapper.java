package org.datamate.identity.application.mapper.role;

import org.datamate.identity.application.dto.role.RoleDto;
import org.datamate.identity.domain.model.Role;

import com.datamate.bedrock.framework.common.pagination.Paged;
import org.springframework.stereotype.Component;

@Component
public class RoleDtoMapper {

    public RoleDto toDto(Role role) {
        if (role == null) return null;
        return new RoleDto(role.getId(), role.getName(), role.getDescription(), role.getStatus());
    }

    public Paged<RoleDto> toPaged(Paged<Role> paged) {
        if (paged == null) return null;
        return paged.map(this::toDto);
    }
}
