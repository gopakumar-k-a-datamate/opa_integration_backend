package org.datamate.identity.user.application.port.in;

import java.util.UUID;

public interface ActivateUserUseCase {
    void activateUser(UUID id, String adminUsername);
}

