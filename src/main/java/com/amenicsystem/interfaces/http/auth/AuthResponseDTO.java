package com.amenicsystem.interfaces.http.auth;

public record AuthResponseDTO(
    String accessToken,
    String tokenType,
    Long expiresIn
) {}
