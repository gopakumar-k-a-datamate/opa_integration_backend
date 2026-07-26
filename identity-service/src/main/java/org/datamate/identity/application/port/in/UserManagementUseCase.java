package org.datamate.identity.application.port.in;

import org.datamate.identity.application.dto.UserDto;
import java.util.List;

public interface UserManagementUseCase extends CreateUserUseCase {
    List<UserDto> listUsers();
}
