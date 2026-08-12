package org.datamate.identity.adapter.in.event;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.identity.shared.event.role.RoleCreatedEvent;
import org.datamate.identity.shared.event.role.RoleUpdatedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class RoleEventListener {

    @EnableLogger
    private Logger log;

    @ApplicationModuleListener
    public void onRoleCreated(RoleCreatedEvent event) {
        if (log != null) {
            log.info("Received RoleCreatedEvent outbox event for role: {}", event.name());
        }
    }

    @ApplicationModuleListener
    public void onRoleUpdated(RoleUpdatedEvent event) {
        if (log != null) {
            log.info("Received RoleUpdatedEvent outbox event for role: {}", event.name());
        }
    }
}
