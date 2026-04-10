package com.cinesystem.interfaces.http.sessao;

public record AssentoResponseDTO(
    Long id,
    String fileira,
    int numero,
    String tipo,
    String status
) {}
