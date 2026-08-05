package org.datamate.identity.adapter.in.event;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.identity.shared.event.user.UserCreatedEvent;
import org.datamate.identity.shared.event.user.UserPasswordResetByAdminEvent;
import org.datamate.identity.shared.event.user.UserPasswordChangedEvent;
import org.datamate.identity.shared.event.user.UserInformationUpdatedEvent;
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

    @ApplicationModuleListener
    public void onUserPasswordResetByAdmin(UserPasswordResetByAdminEvent event) {
        if (log != null) {
            log.info("Received UserPasswordResetByAdminEvent outbox event for user ID: {}, reset by admin: {}", event.aggregateId(), event.resetBy());
        }
    }

    @ApplicationModuleListener
    public void onUserPasswordChanged(UserPasswordChangedEvent event) {
        if (log != null) {
            log.info("Received UserPasswordChangedEvent outbox event for user ID: {}, changed by: {}", event.aggregateId(), event.changedBy());
        }
    }

    @ApplicationModuleListener
    public void onUserInformationUpdated(UserInformationUpdatedEvent event) {
        if (log != null) {
            log.info("Received UserInformationUpdatedEvent outbox event for user ID: {}, updated by: {}", event.aggregateId(), event.updatedBy());
        }
    }
}
