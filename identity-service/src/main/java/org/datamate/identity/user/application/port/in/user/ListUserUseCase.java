package org.datamate.identity.user.application.port.in.user;

import org.datamate.identity.user.application.dto.user.UserResponseDto;
import org.datamate.identity.user.application.query.user.UserSearchCriteria;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;

public interface ListUserUseCase {
    Paged<UserResponseDto> searchUsers(UserSearchCriteria criteria, PageQuery pageQuery);
}
