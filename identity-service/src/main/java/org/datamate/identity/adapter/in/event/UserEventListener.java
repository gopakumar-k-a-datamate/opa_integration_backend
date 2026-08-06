package org.datamate.identity.adapter.in.event;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.identity.shared.event.user.UserCreatedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    @EnableLogger
    private Logger log;

    @ApplicationModuleListener
    public void onUserCreated(UserCreatedEvent event) {
        if (log != null) {
            log.info("Received UserCreatedEvent outbox event for user: {}", event.userName());
        }
    }
}
