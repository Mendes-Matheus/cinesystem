package com.amenicsystem.interfaces.http.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequestDTO(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String senha
) {}
