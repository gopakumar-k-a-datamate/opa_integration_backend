package org.datamate.identity.application.usecase.user;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.user.UpdateUserRolesRequest;
import org.datamate.identity.application.dto.user.UserDto;
import org.datamate.identity.application.mapper.user.UserDtoMapper;
import org.datamate.identity.application.port.in.user.UpdateUserRolesUseCase;
import org.datamate.identity.application.port.out.role.RolePersistencePort;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.domain.exception.role.RoleNotFoundException;
import org.datamate.identity.domain.exception.user.InvalidRoleAssignmentException;
import org.datamate.identity.domain.exception.user.UserNotFoundException;
import org.datamate.identity.domain.model.Role;
import org.datamate.identity.domain.model.User;
import org.datamate.identity.shared.model.RoleStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateUserRolesService implements UpdateUserRolesUseCase {

    private final UserPersistencePort userPort;
    private final RolePersistencePort rolePort;
    private final UserDtoMapper userDtoMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public UserDto updateUserRoles(UUID userId, UpdateUserRolesRequest request, String adminUsername) {
        User user = userPort.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // Validate that all roles exist and are active
        List<Role> allRoles = rolePort.findAll();
        for (String roleName : request.roles()) {
            Role role = allRoles.stream()
                    .filter(r -> r.getName().equals(roleName))
                    .findFirst()
                    .orElseThrow(RoleNotFoundException::new);

            if (role.getStatus() != RoleStatus.ACTIVE) {
                throw new InvalidRoleAssignmentException(roleName);
            }
        }

        User updatedUser = user.assignRoles(request.roles(), adminUsername);
        User savedUser = userPort.save(updatedUser);

        updatedUser.pullEvents().forEach(eventPublisher::publishEvent);

        return userDtoMapper.toDto(savedUser);
    }
}
