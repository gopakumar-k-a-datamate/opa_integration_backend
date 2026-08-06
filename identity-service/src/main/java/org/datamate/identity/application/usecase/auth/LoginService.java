package org.datamate.identity.application.usecase.auth;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.auth.AuthResponse;
import org.datamate.identity.application.dto.auth.LoginRequest;
import org.datamate.identity.application.port.in.auth.LoginUseCase;
import org.datamate.identity.application.port.out.PasswordEncoderPort;
import org.datamate.identity.application.port.out.TokenGeneratorPort;
import org.datamate.identity.application.port.out.user.UserPersistencePort;
import org.datamate.identity.domain.exception.InvalidCredentialsException;
import org.datamate.identity.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    @EnableLogger
    private Logger log;

    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenGeneratorPort tokenGeneratorPort;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login request for user '{}'", request.userName());

        User user = userPersistencePort.findByUserName(request.userName())
                .orElseThrow(() -> {
                    log.warn("Login failed: user '{}' not found", request.userName());
                    return new InvalidCredentialsException("Invalid username or password");
                });

        if (!passwordEncoderPort.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed: incorrect password for user '{}'", request.userName());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String accessToken = tokenGeneratorPort.generateAccessToken(user);
        String refreshToken = tokenGeneratorPort.generateRefreshToken(user);

        log.info("User '{}' authenticated successfully", user.getUserName());
        return new AuthResponse(accessToken, refreshToken, user.getUserName(), user.getEmail());
    }

    @Override
    public void logout(String token) {
        log.info("Processing logout request");
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            tokenGeneratorPort.invalidateToken(jwt);
            log.info("Logout successful: token invalidated");
        } else {
            log.warn("Logout skipped: invalid or missing authorization header");
        }
    }
}
