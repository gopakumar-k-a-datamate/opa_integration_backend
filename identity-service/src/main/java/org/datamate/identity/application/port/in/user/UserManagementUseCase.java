package org.datamate.identity.application.port.in.user;

import org.datamate.identity.application.dto.user.UserDto;

import java.util.List;

public interface UserManagementUseCase extends CreateUserUseCase {
    List<UserDto> listUsers();
}
