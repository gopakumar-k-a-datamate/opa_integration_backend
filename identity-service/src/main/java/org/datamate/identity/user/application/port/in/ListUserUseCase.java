package org.datamate.identity.user.application.port.in;

import org.datamate.identity.user.application.dto.UserResponseDto;
import org.datamate.identity.user.application.query.UserSearchCriteria;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;

public interface ListUserUseCase {
    Paged<UserResponseDto> searchUsers(UserSearchCriteria criteria, PageQuery pageQuery);
}


