package org.datamate.identity.role.application.usecase.role;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.role.application.dto.role.RoleDto;
import org.datamate.identity.role.application.query.role.RoleSearchCriteria;
import org.datamate.identity.role.application.port.in.role.ListRolesUseCase;
import org.datamate.identity.role.application.port.out.role.RolePersistencePort;
import org.datamate.identity.role.application.mapper.role.RoleDtoMapper;
import org.datamate.identity.role.domain.model.role.entity.Role;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import com.datamate.bedrock.framework.common.pagination.PaginationHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListRolesUseService implements ListRolesUseCase {

    @EnableLogger
    private Logger log;

    private final RolePersistencePort rolePort;
    private final RoleDtoMapper roleDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public Paged<RoleDto> listRoles(RoleSearchCriteria criteria, PageQuery pageQuery) {
        log.info("Listing roles with criteria: {} and page query: {}", criteria, pageQuery);
        int validatedPage = PaginationHelper.validatePageNumber(pageQuery.page());
        int validatedSize = PaginationHelper.validateLimit(pageQuery.size());
        PageQuery validatedPageQuery = new PageQuery(validatedPage, validatedSize);

        Paged<Role> rolePaged = rolePort.searchRoles(criteria, validatedPageQuery);
        Paged<RoleDto> result = roleDtoMapper.toPaged(rolePaged);
        log.info("Successfully fetched {} roles of {} total", result.content().size(), result.totalElements());
        return result;
    }
}
