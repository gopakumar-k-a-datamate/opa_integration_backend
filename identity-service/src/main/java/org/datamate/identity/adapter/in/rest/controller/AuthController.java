package org.datamate.identity.adapter.in.rest.controller;

import com.datamate.bedrock.framework.common.auditing.annotation.AuditLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.auth.AuthResponse;
import org.datamate.identity.application.dto.auth.LoginRequest;
import org.datamate.identity.application.port.in.auth.LoginUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;

    @PostMapping("/login")
    @AuditLog(action = "USER_LOGIN", resource = "AUTH", description = "User login attempt", includeArgs = true)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = loginUseCase.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @AuditLog(action = "USER_LOGOUT", resource = "AUTH", description = "User logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        loginUseCase.logout(authHeader);
        return ResponseEntity.ok().build();
    }
}
