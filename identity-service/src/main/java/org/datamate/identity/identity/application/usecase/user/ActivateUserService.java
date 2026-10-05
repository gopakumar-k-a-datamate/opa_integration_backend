package org.datamate.identity.identity.application.usecase.user;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.identity.application.port.in.user.ActivateUserUseCase;
import org.datamate.identity.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.identity.domain.exception.user.UserNotFoundException;
import org.datamate.identity.identity.domain.model.user.entity.User;
import com.datamate.bedrock.framework.common.logging.annotation.LogAction;
import com.datamate.bedrock.framework.common.logging.annotation.LogAttribute;
import com.datamate.bedrock.framework.common.logging.util.LogContext;
import org.datamate.identity.identity.domain.log.IdentityLogKey;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivateUserService implements ActivateUserUseCase {

    @EnableLogger
    private Logger log;

    private final UserPersistencePort userPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @LogAction(
        action = "ACTIVATE_USER",
        attributes = {
            @LogAttribute(key = "targetUserId", value = "#id.toString()")
        }
    )
    public void activateUser(UUID id, String adminUsername) {
        log.info("Starting activation of user ID: {} by admin: {}", id, adminUsername);

        User user = userPort.findById(id).orElseThrow(() -> {
            log.error("User activation failed. User not found for ID: {}", id);
            return new UserNotFoundException();
        });

        User activatedUser = user.activate(adminUsername);
        userPort.save(activatedUser);

        LogContext.put(IdentityLogKey.TARGET_USER_ID, activatedUser.getId().toString());
        LogContext.put(IdentityLogKey.TARGET_USERNAME, activatedUser.getUserName());

        activatedUser.pullEvents().forEach(eventPublisher::publishEvent);

        log.info("Successfully activated user ID: {} by admin: {}", id, adminUsername);
    }
}
