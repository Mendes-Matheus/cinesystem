package com.cinesystem.interfaces.http.ingresso;

import com.cinesystem.domain.pagamento.MetodoPagamento;
import jakarta.validation.constraints.NotNull;

public record IngressoRequestDTO(
    @NotNull Long sessaoId,
    @NotNull Long assentoId,
    @NotNull MetodoPagamento metodoPagamento
) {}
