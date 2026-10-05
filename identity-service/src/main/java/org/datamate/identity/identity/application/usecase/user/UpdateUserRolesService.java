package org.datamate.identity.identity.application.usecase.user;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.identity.application.dto.user.UpdateUserRolesRequest;
import org.datamate.identity.identity.application.dto.user.UserDto;
import org.datamate.identity.identity.application.mapper.user.UserDtoMapper;
import org.datamate.identity.identity.application.port.in.user.UpdateUserRolesUseCase;
import org.datamate.identity.identity.application.port.out.role.RolePersistencePort;
import org.datamate.identity.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.identity.domain.exception.role.RoleNotFoundException;
import org.datamate.identity.identity.domain.exception.user.InvalidRoleAssignmentException;
import org.datamate.identity.identity.domain.exception.user.UserNotFoundException;
import org.datamate.identity.identity.domain.model.role.entity.Role;
import org.datamate.identity.identity.domain.model.user.entity.User;
import org.datamate.identity.identity.domain.model.role.enums.RoleStatus;
import com.datamate.bedrock.framework.common.logging.annotation.LogAction;
import com.datamate.bedrock.framework.common.logging.annotation.LogAttribute;
import com.datamate.bedrock.framework.common.logging.util.LogContext;
import org.datamate.identity.identity.domain.log.IdentityLogKey;
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
    @LogAction(
        action = "ASSIGN_ROLE",
        attributes = {
            @LogAttribute(key = "targetUserId", value = "#userId.toString()")
        }
    )
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

        LogContext.put(IdentityLogKey.TARGET_USER_ID, savedUser.getId().toString());
        LogContext.put(IdentityLogKey.TARGET_USERNAME, savedUser.getUserName());
        LogContext.put(IdentityLogKey.ROLES_ASSIGNED, request.roles());

        updatedUser.pullEvents().forEach(eventPublisher::publishEvent);

        return userDtoMapper.toDto(savedUser);
    }
}
