package org.datamate.identity.application.usecase.user;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.user.UserDto;
import org.datamate.identity.application.mapper.user.UserDtoMapper;
import org.datamate.identity.application.port.in.LoginUseCase;
import org.datamate.identity.application.port.in.user.UserManagementUseCase;
import org.datamate.identity.application.port.out.PasswordEncoderPort;
import org.datamate.identity.application.port.out.SecurityContextPort;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.domain.exception.user.UserAlreadyExistsException;
import org.datamate.identity.domain.model.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserManagementUseCase {
    private final UserPersistencePort userPort;
    private final UserDtoMapper userDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        return userPort.findAll().stream()
                .map(userDtoMapper::toDto)
                .toList();
    }
}
