package com.amenicsystem.application.port.out;

import java.math.BigDecimal;


/**
 * Dados necessários para criar uma Preference no Mercado Pago (Checkout Pro).
 * Não carrega mais dados de cartão — a escolha do método de pagamento é feita
 * pelo usuário diretamente na página do Mercado Pago (initPoint).
 */
public record CriarPagamentoRequest(

        /** ID do ingresso gerado internamente — usado como externalReference. */
        Long ingressoId,

        /** Título do item exibido na tela de pagamento do MP (ex: "Ingresso – Duna 2 – 21/05 19h"). */
        String tituloItem,

        /** Valor do ingresso. */
        BigDecimal valor,

        /** E-mail do comprador — preenchido automaticamente no checkout do MP. */
        String emailPagador,

        /**
         * URL de retorno após pagamento aprovado (back_url.success).
         * Normalmente lida do application.yml via @Value no adapter.
         * Pode ser null — o adapter usará o valor configurado.
         */
        String backUrlSucesso,

        /**
         * URL de retorno após pagamento falhou (back_url.failure).
         */
        String backUrlFalha,

        /**
         * URL de retorno para pagamentos pendentes (back_url.pending).
         */
        String backUrlPendente,

        /**
         * URL para notificações IPN/webhook do Mercado Pago.
         * Se null, o adapter usará o valor configurado em application.yml.
         */
        String notificationUrl
) {}

