package org.datamate.identity.application.port.out;

import org.datamate.identity.domain.model.User;

import java.util.List;

public interface TokenGeneratorPort {
    String generateToken(User user, List<String> roles);
}
