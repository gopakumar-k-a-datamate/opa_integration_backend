package org.datamate.identity.adapter.in.rest.controller;

import lombok.RequiredArgsConstructor;
import org.datamate.identity.application.dto.UserDto;
import org.datamate.identity.application.port.in.UserManagementUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserManagementUseCase userManagementUseCase;

    @GetMapping
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(userManagementUseCase.listUsers());
    }
}
