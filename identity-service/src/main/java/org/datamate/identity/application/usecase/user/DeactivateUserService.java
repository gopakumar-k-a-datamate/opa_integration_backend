package org.datamate.identity.application.usecase.user;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.port.in.user.DeactivateUserUseCase;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.domain.exception.user.UserNotFoundException;
import org.datamate.identity.domain.model.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeactivateUserService implements DeactivateUserUseCase {

    @EnableLogger
    private Logger log;

    private final UserPersistencePort userPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void deactivateUser(UUID id, String adminUsername) {
        log.info("Starting deactivation of user ID: {} by admin: {}", id, adminUsername);

        User user = userPort.findById(id).orElseThrow(() -> {
            log.error("User deactivation failed. User not found for ID: {}", id);
            return new UserNotFoundException();
        });

        User deactivatedUser = user.deactivate(adminUsername);
        userPort.save(deactivatedUser);

        deactivatedUser.pullEvents().forEach(eventPublisher::publishEvent);

        log.info("Successfully deactivated user ID: {} by admin: {}", id, adminUsername);
    }
}
