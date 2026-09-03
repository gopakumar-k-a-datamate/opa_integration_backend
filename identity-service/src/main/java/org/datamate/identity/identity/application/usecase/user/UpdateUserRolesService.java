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

            if ("SECURITY_ADMIN".equalsIgnoreCase(roleName)) {
                boolean isCallerSecurityAdmin = adminUsername != null && (
                        adminUsername.equalsIgnoreCase("admin@123.com") ||
                        userPort.findByUserNameOrEmail(adminUsername, adminUsername)
                                .map(u -> u.getRoles().contains("SECURITY_ADMIN"))
                                .orElse(false)
                );
                if (!isCallerSecurityAdmin) {
                    throw new InvalidRoleAssignmentException("Access Denied: Only a SECURITY_ADMIN can assign the SECURITY_ADMIN role.", roleName);
                }
            }
        }

        User updatedUser = user.assignRoles(request.roles(), adminUsername);
        User savedUser = userPort.save(updatedUser);

        updatedUser.pullEvents().forEach(eventPublisher::publishEvent);

        return userDtoMapper.toDto(savedUser);
    }
}
