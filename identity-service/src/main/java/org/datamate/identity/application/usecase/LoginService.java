package org.datamate.identity.application.usecase;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.AuthResponse;
import org.datamate.identity.application.dto.LoginRequest;
import org.datamate.identity.application.port.in.LoginUseCase;
import org.datamate.identity.application.port.out.PasswordEncoderPort;
import org.datamate.identity.application.port.out.TokenGeneratorPort;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.domain.exception.InvalidCredentialsException;
import org.datamate.identity.domain.model.User;
import org.springframework.stereotype.Service;
import org.datamate.identity.application.port.out.RolePersistencePort;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    @EnableLogger
    private Logger log;

    private final UserPersistencePort userPort;
    private final RolePersistencePort rolePort;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenGeneratorPort tokenGenerator;

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user '{}'", request.userName());
        User user = userPort.findByUserName(request.userName())
            .orElseThrow(() -> {
                log.warn("Login failed: user '{}' not found", request.userName());
                return new InvalidCredentialsException("Invalid username or password");
            });

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed: invalid password for user '{}'", request.userName());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        List<String> roles = rolePort.findRoleNamesByUserId(user.getId());
        log.info("Login successful for user '{}'", request.userName());
        return new AuthResponse(tokenGenerator.generateToken(user, roles));
    }
}
