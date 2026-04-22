package com.cinesystem.interfaces.http.ingresso;

import com.cinesystem.domain.ingresso.TipoIngresso;
import com.cinesystem.domain.pagamento.MetodoPagamento;
import jakarta.validation.constraints.NotNull;

public record IngressoRequestDTO(
        @NotNull(message = "O ID da sessão é obrigatório")
        Long sessaoId,

        @NotNull(message = "O ID do assento é obrigatório")
        Long assentoId,

        @NotNull(message = "O tipo de ingresso (INTEIRA ou MEIA) é obrigatório")
        TipoIngresso tipo,

        @NotNull(message = "O metódo de pagamento é obrigatório")
        MetodoPagamento metodoPagamento
) {}