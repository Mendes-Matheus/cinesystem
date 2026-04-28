package com.cinesystem.infrastructure.pagamento;

import com.cinesystem.application.port.out.PagamentoGatewayPort;
import com.cinesystem.application.port.out.TransacaoGatewayResult;
import com.cinesystem.domain.pagamento.Pagamento;
import com.cinesystem.domain.shared.DomainException;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.resources.payment.Payment;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoGatewayAdapter implements PagamentoGatewayPort {

    @Override
    public TransacaoGatewayResult processarPagamentoPix(Pagamento pagamento, String emailCliente) {
        try {
            PaymentClient client = new PaymentClient();
            
            PaymentCreateRequest createRequest = PaymentCreateRequest.builder()
                    .transactionAmount(pagamento.getValor())
                    .description("Ingresso ID: " + pagamento.getIngressoId())
                    .paymentMethodId("pix")
                    .payer(PaymentPayerRequest.builder()
                            .email(emailCliente)
                            .build())
                    .build();

            Payment payment = client.create(createRequest);
            
            if (payment.getPointOfInteraction() == null || payment.getPointOfInteraction().getTransactionData() == null) {
                throw new DomainException("Erro ao gerar QR Code PIX no Mercado Pago");
            }

            return new TransacaoGatewayResult(
                    String.valueOf(payment.getId()),
                    payment.getPointOfInteraction().getTransactionData().getQrCode(),
                    payment.getPointOfInteraction().getTransactionData().getQrCodeBase64()
            );

        } catch (Exception e) {
            throw new DomainException("Erro de comunicação com o Mercado Pago: " + e.getMessage());
        }
    }
}
