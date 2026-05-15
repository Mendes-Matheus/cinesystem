package com.amenicsystem.infrastructure.payment.mercadopago;

import com.amenicsystem.application.port.out.ConsultaPagamentoResult;
import com.amenicsystem.application.port.out.CriarPagamentoRequest;
import com.amenicsystem.application.port.out.GatewayPagamentoResult;
import com.amenicsystem.application.port.out.PagamentoGatewayPort;
import com.amenicsystem.domain.shared.GatewayException;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter para o Mercado Pago usando Checkout Pro (Preference API).
 *
 * Fluxo:
 *   1. criarPreference() → cria uma Preference com o ingresso como item
 *      → retorna initPoint (URL para redirecionar o usuário)
 *   2. O usuário paga na página do MP (escolhe Pix, cartão, boleto, etc.)
 *   3. O MP notifica via webhook com o payment ID
 *   4. consultarStatusPagamento() → busca o status real do pagamento pelo ID
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoAdapter implements PagamentoGatewayPort {

    @Value("${mercado-pago.back-url.success}")
    private String backUrlSucesso;

    @Value("${mercado-pago.back-url.fail}")
    private String backUrlFalha;

    @Value("${mercado-pago.back-url.pending}")
    private String backUrlPendente;

    @Value("${mercado-pago.notification-url}")
    private String notificationUrl;

    @Override
    public GatewayPagamentoResult criarPreference(CriarPagamentoRequest request) {
        log.info("Criando Preference no Mercado Pago para ingressoId: {}", request.ingressoId());

        try {
            PreferenceClient client = new PreferenceClient();

            // Item = o ingresso
            var item = PreferenceItemRequest.builder()
                    .id("ingresso-" + request.ingressoId())
                    .title(request.tituloItem())
                    .quantity(1)
                    .unitPrice(request.valor())
                    .build();

            // Pagador — e-mail preenche o checkout automaticamente
            var payer = PreferencePayerRequest.builder()
                    .email(request.emailPagador())
                    .build();

            String successUrl = request.backUrlSucesso();
            if (successUrl == null || successUrl.isBlank()) successUrl = backUrlSucesso;
            if (successUrl == null || successUrl.isBlank()) successUrl = "https://pogo-spinach-protract.ngrok-free.dev/pagamento/sucesso";

            String failureUrl = request.backUrlFalha();
            if (failureUrl == null || failureUrl.isBlank()) failureUrl = backUrlFalha;
            if (failureUrl == null || failureUrl.isBlank()) failureUrl = "https://pogo-spinach-protract.ngrok-free.dev/pagamento/falha";

            String pendingUrl = request.backUrlPendente();
            if (pendingUrl == null || pendingUrl.isBlank()) pendingUrl = backUrlPendente;
            if (pendingUrl == null || pendingUrl.isBlank()) pendingUrl = "https://pogo-spinach-protract.ngrok-free.dev/pagamento/pendente";

            // Notification URL — onde o MP enviará o webhook de confirmação
            String notifUrl = request.notificationUrl();
            if (notifUrl == null || notifUrl.isBlank()) notifUrl = notificationUrl;

            // URLs de retorno após o pagamento (configuradas no application.yml)
            var backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .failure(failureUrl)
                    .pending(pendingUrl)
                    .build();

            log.info("Valores das backUrls - Success: '{}', Failure: '{}', Pending: '{}', NotificationUrl: '{}'",
                    successUrl, failureUrl, pendingUrl, notifUrl);

            var preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(item))
                    .payer(payer)
                    .backUrls(backUrls)
                    .notificationUrl(notifUrl)
                    .autoReturn("approved")
                    // Vincula ao ingresso — usado para correlacionar no webhook
                    .externalReference("ingresso-" + request.ingressoId())
                    .build();

            var preference = client.create(preferenceRequest);

            log.info("Preference criada. ID: {}, InitPoint: {}",
                    preference.getId(), preference.getInitPoint());

            return new GatewayPagamentoResult(
                    preference.getId(),
                    preference.getInitPoint()
            );

        } catch (MPApiException e) {
            log.error("Erro da API do Mercado Pago ao criar preference para ingressoId {}: " +
                            "HTTP {}, Resposta: {}",
                    request.ingressoId(), e.getStatusCode(), e.getApiResponse().getContent(), e);
            throw new GatewayException(
                    "Erro da API do Mercado Pago: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            log.error("Erro do SDK do Mercado Pago ao criar preference para ingressoId {}: {}",
                    request.ingressoId(), e.getMessage(), e);
            throw new GatewayException("Erro do SDK do Mercado Pago: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao criar preference para ingressoId {}: {}",
                    request.ingressoId(), e.getMessage(), e);
            throw new GatewayException("Erro inesperado ao criar preference.", e);
        }
    }

    @Override
    public ConsultaPagamentoResult consultarStatusPagamento(String paymentId) {
        log.info("Consultando status do pagamento {} no Mercado Pago", paymentId);

        try {
            PaymentClient client = new PaymentClient();
            var payment = client.get(Long.parseLong(paymentId));

            if (payment == null) {
                log.warn("Pagamento não encontrado no Mercado Pago para ID: {}", paymentId);
                throw new GatewayException("Pagamento não encontrado com ID: " + paymentId);
            }

            log.debug("Status do pagamento {}: {}", paymentId, payment.getStatus());
            return new ConsultaPagamentoResult(payment.getStatus(), payment.getExternalReference());

        } catch (NumberFormatException e) {
            log.error("Formato inválido de paymentId: {}", paymentId, e);
            throw new GatewayException("Formato inválido de paymentId: " + paymentId, e);
        } catch (MPApiException e) {
            if (e.getStatusCode() == 404) {
                log.warn("Pagamento não encontrado no Mercado Pago (provável teste de webhook ou ID inválido). ID: {}, Resposta: {}", 
                        paymentId, e.getApiResponse().getContent());
                throw new GatewayException("Pagamento não encontrado com ID: " + paymentId, e);
            }
            
            log.error("Erro da API do Mercado Pago ao consultar pagamento {}: HTTP {}, Resposta: {}",
                    paymentId, e.getStatusCode(), e.getApiResponse().getContent(), e);
            throw new GatewayException(
                    "Erro da API do Mercado Pago: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            log.error("Erro do SDK do Mercado Pago ao consultar pagamento {}: {}",
                    paymentId, e.getMessage(), e);
            throw new GatewayException("Erro do SDK do Mercado Pago: " + e.getMessage(), e);
        } catch (GatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao consultar pagamento {}: {}", paymentId, e.getMessage(), e);
            throw new GatewayException("Erro inesperado ao consultar pagamento.", e);
        }
    }
}

