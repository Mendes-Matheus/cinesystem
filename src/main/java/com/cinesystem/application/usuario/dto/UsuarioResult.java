package com.cinesystem.application.usuario.dto;

import java.time.LocalDateTime;

public record UsuarioResult(
    Long id, 
    String nome, 
    String email,
    String role, 
    boolean ativo, 
    LocalDateTime criadoEm
) {}
