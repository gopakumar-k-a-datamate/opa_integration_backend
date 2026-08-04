package org.datamate.identity.application.usecase;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.command.LoginCommand;
import org.datamate.identity.application.dto.AuthResponse;
import org.datamate.identity.application.port.in.LoginUseCase;
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

    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenGeneratorPort tokenGeneratorPort;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginCommand command) {
        User user = userPersistencePort.findByUserName(command.userName())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoderPort.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String accessToken = tokenGeneratorPort.generateAccessToken(user);
        String refreshToken = tokenGeneratorPort.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, user.getUserName(), user.getEmail());
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            tokenGeneratorPort.invalidateToken(jwt);
        }
    }
}
