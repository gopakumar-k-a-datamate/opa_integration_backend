package org.datamate.identity.adapter.out.security;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.identity.application.port.in.LoginUseCase;
import org.datamate.identity.application.port.out.PasswordEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoderPort {

    @EnableLogger
    private Logger log;

    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(String rawPassword) {
        String encoded = passwordEncoder.encode(rawPassword);
        log.debug("Password encoded successfully");
        return encoded;
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        log.debug("Password match evaluated for encoded password");
        return matches;
    }
}
