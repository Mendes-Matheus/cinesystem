package com.cinesystem.interfaces.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDTO(
    String codigo,
    String mensagem,
    LocalDateTime timestamp,
    List<String> detalhes
) {
    public ErrorResponseDTO(String codigo, String mensagem) {
        this(codigo, mensagem, LocalDateTime.now(), List.of());
    }
}
