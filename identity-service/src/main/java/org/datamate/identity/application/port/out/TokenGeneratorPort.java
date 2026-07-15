package org.datamate.identity.application.port.out;

import org.datamate.identity.domain.model.User;

public interface TokenGeneratorPort {
    String generateToken(User user);
}
