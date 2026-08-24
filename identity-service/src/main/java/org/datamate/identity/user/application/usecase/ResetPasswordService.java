package org.datamate.identity.user.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.user.application.dto.ResetPasswordRequest;
import org.datamate.identity.user.application.dto.UserDto;
import org.datamate.identity.user.application.mapper.UserDtoMapper;
import org.datamate.identity.user.application.port.in.ResetPasswordUseCase;
import org.datamate.identity.auth.application.port.out.PasswordEncoderPort;
import org.datamate.identity.auth.application.port.out.SecurityContextPort;
import org.datamate.identity.user.application.port.out.UserPersistencePort;
import org.datamate.identity.user.domain.exception.UserNotFoundException;
import org.datamate.identity.user.domain.model.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

    @EnableLogger
    private Logger log;

    private final UserPersistencePort userPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final SecurityContextPort securityContextPort;
    private final ApplicationEventPublisher eventPublisher;
    private final UserDtoMapper userDtoMapper;

    @Override
    @Transactional
    public UserDto resetPassword(UUID userId, ResetPasswordRequest request) {
        log.info("Resetting password for user ID: {}", userId);
        
        User user = userPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        String adminUsername = securityContextPort.getCurrentUsername();
        String newPasswordHash = passwordEncoderPort.encode(request.newPassword());

        User updatedUser = user.resetPassword(newPasswordHash, adminUsername);
        User savedUser = userPort.save(updatedUser);

        updatedUser.pullEvents().forEach(eventPublisher::publishEvent);

        log.info("Password successfully reset by admin '{}' for user '{}'", adminUsername, user.getUserName());
        return userDtoMapper.toDto(savedUser);
    }
}


