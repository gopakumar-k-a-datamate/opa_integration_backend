package org.datamate.identity.adapter.in.rest.controller;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.AuthResponse;
import org.datamate.identity.application.dto.LoginRequest;
import org.datamate.identity.application.port.in.LoginUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final LoginUseCase loginUseCase;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUseCase.login(request));
    }
}
