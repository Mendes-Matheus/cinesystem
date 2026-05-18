package com.amenicsystem.interfaces.http.pagamento.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request para iniciar pagamento via Checkout Pro do Mercado Pago.
 *
 * O método de pagamento (Pix, cartão, boleto) não é mais informado aqui —
 * o usuário o escolhe diretamente na página do Mercado Pago após o redirecionamento.
 */
public record IniciarPagamentoRequestDTO(

        @NotNull(message = "sessaoId é obrigatório")
        Long sessaoId,

        @NotNull(message = "assentoId é obrigatório")
        Long assentoId,

        @NotBlank(message = "emailPagador é obrigatório")
        @Email(message = "emailPagador deve ser um e-mail válido")
        String emailPagador
) {}

