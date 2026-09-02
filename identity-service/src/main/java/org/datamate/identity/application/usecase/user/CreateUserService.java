package org.datamate.identity.application.usecase.user;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.user.CreateUserRequest;
import org.datamate.identity.application.dto.user.UserDto;
import org.datamate.identity.application.mapper.user.UserDtoMapper;
import org.datamate.identity.application.port.in.user.CreateUserUseCase;
import org.datamate.identity.application.port.out.PasswordEncoderPort;
import org.datamate.identity.application.port.out.SecurityContextPort;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.domain.exception.user.UserAlreadyExistsException;
import org.datamate.identity.application.port.out.role.RolePersistencePort;
import org.datamate.identity.domain.model.Role;
import org.datamate.identity.shared.model.RoleStatus;
import org.datamate.identity.domain.exception.role.RoleNotFoundException;
import org.datamate.identity.domain.exception.user.InvalidRoleAssignmentException;
import org.datamate.identity.domain.model.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateUserService implements CreateUserUseCase {

    @EnableLogger
    private Logger log;

    private final UserPersistencePort userPort;
    private final RolePersistencePort rolePort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final SecurityContextPort securityContextPort;
    private final ApplicationEventPublisher eventPublisher;
    private final UserDtoMapper userDtoMapper;

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        log.info("Creating user '{}'", request.userName());
        if (userPort.existsByUserName(request.userName())) {
            log.warn("User creation failed: username '{}' already exists", request.userName());
            throw new UserAlreadyExistsException();
        }

        if (userPort.existsByEmail(request.email())) {
            log.warn("User creation failed: email '{}' already exists", request.email());
            throw new UserAlreadyExistsException();
        }

        // Validate roles if provided
        if (request.roles() != null && !request.roles().isEmpty()) {
            List<Role> assignedRoles = rolePort.findAllByNameIn(request.roles());
            long uniqueRequestedRolesCount = request.roles().stream().distinct().count();
            if (assignedRoles.size() != uniqueRequestedRolesCount) {
                throw new RoleNotFoundException();
            }
            for (Role role : assignedRoles) {
                if (role.getStatus() != RoleStatus.ACTIVE) {
                    throw new InvalidRoleAssignmentException(role.getName());
                }
            }
        }

        String passwordHash = passwordEncoderPort.encode(request.password());
        String createdBy = securityContextPort.getCurrentUsername();

        User newUser = User.create(
                request.userName(),
                request.email(),
                request.phoneNumber(),
                passwordHash,
                request.firstName(),
                request.lastName(),
                request.referenceSystem(),
                request.referenceValue(),
                request.roles(),
                createdBy
        );

        User savedUser = userPort.save(newUser);

        newUser.pullEvents().forEach(eventPublisher::publishEvent);

        log.info("User '{}' created with id {}", request.userName(), savedUser.getId());
        return userDtoMapper.toDto(savedUser);
    }

}
