package org.datamate.identity.application.dto;

import java.util.UUID;

public record AuthResponse(String token, UUID userId) {}
