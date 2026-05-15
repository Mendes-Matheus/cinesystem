package com.amenicsystem.application.pagamento.dto;

/**
 * Command para iniciar o fluxo de pagamento via Mercado Pago (Checkout Pro).
 *
 * Não carrega mais MetodoPagamento nem DadosCartaoCommand — o método de pagamento
 * é escolhido pelo usuário na página do Mercado Pago após o redirecionamento.
 */
public record IniciarPagamentoCommand(
        Long sessaoId,
        Long assentoId,
        Long usuarioId,
        String emailPagador,
        String guestId
) {}
