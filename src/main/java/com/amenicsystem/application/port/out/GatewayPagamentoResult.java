package com.amenicsystem.application.port.out;

/**
 * Resultado da criação de uma Preference no Mercado Pago (Checkout Pro).
 *
 * O adapter retorna o ID da preference e a URL de redirecionamento (initPoint).
 * O frontend deve redirecionar o usuário para {@code redirectUrl} para concluir o pagamento.
 *
 * Campos de QR code, boleto e dados de cartão foram removidos — esses dados
 * são gerenciados diretamente pelo Checkout Pro do Mercado Pago.
 */
public record GatewayPagamentoResult(

        /**
         * ID da preference gerada no Mercado Pago.
         * Armazenado como transacaoExternaId no Pagamento.
         */
        String preferenceId,

        /**
         * URL para a página de pagamento do Mercado Pago (init_point).
         * Deve ser retornada ao frontend para redirecionamento.
         */
        String redirectUrl
) {}

