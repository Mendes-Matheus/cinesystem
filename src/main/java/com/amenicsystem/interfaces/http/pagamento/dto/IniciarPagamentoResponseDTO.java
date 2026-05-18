package com.amenicsystem.interfaces.http.pagamento.dto;

/**
 * Resposta ao iniciar um pagamento.
 *
 * O frontend deve redirecionar o usuário para {@code redirectUrl} imediatamente.
 * O status do ingresso será atualizado para ATIVO após o webhook de aprovação.
 */
public record IniciarPagamentoResponseDTO(
        Long ingressoId,
        String codigoIngresso,

        /** PENDENTE_PAGAMENTO — confirmado apenas após webhook do MP. */
        String statusIngresso,

        /** ID da preference criada no MP. */
        String preferenceId,

        /** PENDENTE — confirmado apenas após webhook do MP. */
        String statusPagamento,

        /**
         * URL de redirecionamento para o Checkout Pro do Mercado Pago.
         * O frontend deve redirecionar o usuário para esta URL.
         */
        String redirectUrl
) {}

