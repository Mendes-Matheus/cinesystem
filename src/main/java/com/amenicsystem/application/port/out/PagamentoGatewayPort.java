package com.amenicsystem.application.port.out;

import com.amenicsystem.domain.shared.GatewayException;

/**
 * Porta de saída para comunicação com o gateway de pagamento (Mercado Pago).
 *
 * Ambos os métodos lançam {@link GatewayException} em caso de falha de comunicação,
 * permitindo que o use case trate o erro sem depender de detalhes do SDK.
 */
public interface PagamentoGatewayPort {

    /**
     * Cria uma Preference no Mercado Pago (Checkout Pro) e retorna a URL de redirecionamento.
     *
     * @param request dados do ingresso e do comprador
     * @return preferenceId e initPoint URL
     * @throws GatewayException se o MP retornar erro ou a comunicação falhar
     */
    GatewayPagamentoResult criarPreference(CriarPagamentoRequest request);

    /**
     * Consulta o status atual de um pagamento no Mercado Pago pelo ID do pagamento
     * (não da preference — o webhook envia o ID do pagamento efetivo).
     *
     * @param paymentId ID numérico do pagamento retornado pelo webhook do MP
     * @return ConsultaPagamentoResult com o status e a referência externa
     * @throws GatewayException se a consulta falhar
     */
    ConsultaPagamentoResult consultarStatusPagamento(String paymentId);
}

