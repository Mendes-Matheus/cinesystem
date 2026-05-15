package com.amenicsystem.application.pagamento.dto;

/**
 * Resultado do caso de uso IniciarPagamento.
 *
 * O campo principal para o frontend é {@code redirectUrl}: a URL do Checkout Pro
 * do Mercado Pago para onde o usuário deve ser redirecionado.
 */
public record IniciarPagamentoResult(
        Long ingressoId,
        String codigoIngresso,

        /** Sempre PENDENTE_PAGAMENTO neste fluxo — a confirmação vem via webhook. */
        String statusIngresso,

        /** ID da preference criada no MP (armazenada como transacaoExternaId). */
        String preferenceId,

        /** PENDENTE — o pagamento só é confirmado após o webhook. */
        String statusPagamento,

        /**
         * URL de redirecionamento para o Checkout Pro do Mercado Pago.
         * O frontend deve redirecionar o usuário para esta URL imediatamente.
         */
        String redirectUrl
) {}

