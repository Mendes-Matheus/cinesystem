package com.cinesystem.interfaces.http.auth;

public record AuthResponseDTO(
    String accessToken,
    String tokenType,
    Long expiresIn
) {}
