package org.datamate.identity.user.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.user.application.dto.ChangePasswordRequest;
import org.datamate.identity.user.application.dto.UserDto;
import org.datamate.identity.user.application.mapper.UserDtoMapper;
import org.datamate.identity.user.application.port.in.ChangePasswordUseCase;
import org.datamate.identity.auth.application.port.out.PasswordEncoderPort;
import org.datamate.identity.auth.application.port.out.SecurityContextPort;
import org.datamate.identity.user.application.port.out.UserPersistencePort;
import org.datamate.identity.user.domain.exception.PasswordMismatchException;
import org.datamate.identity.user.domain.exception.UserAccessDeniedException;
import org.datamate.identity.user.domain.exception.UserNotFoundException;
import org.datamate.identity.user.domain.model.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangePasswordService implements ChangePasswordUseCase {

    @EnableLogger
    private Logger log;

    private final UserPersistencePort userPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final SecurityContextPort securityContextPort;
    private final ApplicationEventPublisher eventPublisher;
    private final UserDtoMapper userDtoMapper;

    @Override
    @Transactional
    public UserDto changePassword(UUID userId, ChangePasswordRequest request) {
        log.info("Changing password for user ID: {}", userId);

        User user = userPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        // Security check: Verify logged in username matches the target user's username
        String loggedInUsername = securityContextPort.getCurrentUsername();
        if (!user.getUserName().equals(loggedInUsername)) {
            log.warn("Access Denied: User '{}' tried to change password for User '{}'", loggedInUsername, user.getUserName());
            throw new UserAccessDeniedException();
        }

        // Verify old password
        if (!passwordEncoderPort.matches(request.oldPassword(), user.getPasswordHash())) {
            log.warn("Change password failed: old password does not match for user '{}'", user.getUserName());
            throw new PasswordMismatchException();
        }

        String username = user.getUserName();
        String newPasswordHash = passwordEncoderPort.encode(request.newPassword());

        User updatedUser = user.changePassword(newPasswordHash, username);
        User savedUser = userPort.save(updatedUser);

        updatedUser.pullEvents().forEach(eventPublisher::publishEvent);

        log.info("Password successfully changed by user '{}'", user.getUserName());
        return userDtoMapper.toDto(savedUser);
    }
}


