package org.datamate.identity.application.usecase;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.AuthResponse;
import org.datamate.identity.application.dto.LoginRequest;
import org.datamate.identity.application.port.in.LoginUseCase;
import org.datamate.identity.application.port.out.PasswordEncoderPort;
import org.datamate.identity.application.port.out.TokenGeneratorPort;
import org.datamate.identity.application.port.out.UserPersistencePort;
import org.datamate.identity.domain.exception.InvalidCredentialsException;
import org.datamate.identity.domain.model.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {
    private final UserPersistencePort userPort;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenGeneratorPort tokenGenerator;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userPort.findByEmail(request.email())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return new AuthResponse(tokenGenerator.generateToken(user), user.getId());
    }
}
