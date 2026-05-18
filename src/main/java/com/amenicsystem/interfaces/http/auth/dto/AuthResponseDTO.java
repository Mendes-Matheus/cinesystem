package com.amenicsystem.interfaces.http.auth.dto;

public record AuthResponseDTO(
    String accessToken,
    String tokenType,
    Long expiresIn
) {}
