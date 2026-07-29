package org.datamate.identity.application.port.in.user;

import org.datamate.identity.application.dto.user.UserDto;
import org.datamate.identity.application.dto.user.UserResponseDto;
import org.datamate.identity.application.dto.user.UserSearchCriteria;
import com.datamate.bedrock.framework.common.pagination.Paged;
import com.datamate.bedrock.framework.common.pagination.PageQuery;
import java.util.List;

public interface UserManagementUseCase {
    List<UserDto> listUsers();
    Paged<UserResponseDto> searchUsers(UserSearchCriteria criteria, PageQuery pageQuery);
}
