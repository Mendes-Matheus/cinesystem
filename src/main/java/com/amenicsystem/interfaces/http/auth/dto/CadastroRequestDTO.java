package com.amenicsystem.interfaces.http.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroRequestDTO(
    @NotBlank String nome,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String senha
) {}
