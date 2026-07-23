package org.datamate.identity.application.usecase;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.UserDto;
import org.datamate.identity.application.port.in.UserManagementUseCase;
import org.datamate.identity.application.port.out.UserPersistencePort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserManagementUseCase {
    private final UserPersistencePort userPort;

    @Override
    public List<UserDto> listUsers() {
        return userPort.findAll().stream()
                .map(u -> new UserDto(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName()))
                .toList();
    }
}
