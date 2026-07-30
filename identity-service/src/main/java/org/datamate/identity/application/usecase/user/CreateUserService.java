package org.datamate.identity.application.usecase.user;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.command.user.CreateUserCommand;
import org.datamate.identity.application.dto.user.UserDto;
import org.datamate.identity.application.mapper.user.UserDtoMapper;
import org.datamate.identity.application.port.in.user.CreateUserUseCase;
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
public class CreateUserService implements CreateUserUseCase {
    private final UserPersistencePort userPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final SecurityContextPort securityContextPort;
    private final ApplicationEventPublisher eventPublisher;
    private final UserDtoMapper userDtoMapper;

    @Override
    @Transactional
    public UserDto createUser(CreateUserCommand command) {
        if (userPort.existsByUserName(command.userName())) {
            throw new UserAlreadyExistsException("Username '" + command.userName() + "' already exists");
        }

        if (userPort.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("Email '" + command.email() + "' already exists");
        }

        String passwordHash = passwordEncoderPort.encode(command.password());
        String createdBy = securityContextPort.getCurrentUsername();

        User newUser = User.create(
                command.userName(),
                command.email(),
                command.phoneNumber(),
                passwordHash,
                command.firstName(),
                command.lastName(),
                command.referenceSystem(),
                command.referenceValue(),
                createdBy
        );

        User savedUser = userPort.save(newUser);

        newUser.pullEvents().forEach(eventPublisher::publishEvent);

        return userDtoMapper.toDto(savedUser);
    }

}
