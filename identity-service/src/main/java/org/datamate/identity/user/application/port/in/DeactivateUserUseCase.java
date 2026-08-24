package org.datamate.identity.user.application.port.in;

import java.util.UUID;

public interface DeactivateUserUseCase {
    void deactivateUser(UUID id, String adminUsername);
}

