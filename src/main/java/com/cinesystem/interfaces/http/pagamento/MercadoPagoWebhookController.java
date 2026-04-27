package com.cinesystem.interfaces.http.pagamento;

import com.cinesystem.application.ingresso.usecase.ConfirmarPagamentoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoWebhookController {

    private final ConfirmarPagamentoUseCase confirmarPagamentoUseCase;

    @PostMapping
    public ResponseEntity<Void> receberNotificacao(@RequestBody Map<String, Object> payload) {
        // Lógica simplificada: extrair o ID da transação do JSON do Mercado Pago
        // Geralmente vem em data.id ou action dependendo do evento
        if ("payment.updated".equals(payload.get("action"))) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String paymentId = String.valueOf(data.get("id"));

            confirmarPagamentoUseCase.execute(paymentId);
        }

        return ResponseEntity.ok().build();
    }
}