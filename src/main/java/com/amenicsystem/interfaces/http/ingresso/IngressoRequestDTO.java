package com.amenicsystem.interfaces.http.ingresso;

import com.amenicsystem.domain.ingresso.TipoIngresso;
import com.amenicsystem.domain.pagamento.MetodoPagamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IngressoRequestDTO(
        @NotNull(message = "O ID da sessão é obrigatório")
        Long sessaoId,

        @NotNull(message = "O ID do assento é obrigatório")
        Long assentoId,

        @NotNull(message = "O tipo de ingresso (INTEIRA ou MEIA) é obrigatório")
        TipoIngresso tipo,

        @NotNull(message = "O metódo de pagamento é obrigatório")
        MetodoPagamento metodoPagamento,

        @NotBlank(message = "O CPF é obrigatório")
        String cpfCliente,

        @NotBlank(message = "O nome é obrigatório")
        String nomeCliente
) {}