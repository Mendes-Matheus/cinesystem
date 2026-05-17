package com.amenicsystem.application.pagamento.usecase;

import com.amenicsystem.application.pagamento.dto.WebhookPagamentoCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para confirmação de pagamento via webhook.
 *
 * <p>Thin delegator: recebe o command validado e delega ao
 * {@link WebhookProcessingService} que coordena toda a lógica transacional.</p>
 *
 * <p>Mantido como use case para preservar a arquitetura de ports &amp; adapters
 * e permitir que o controller continue dependendo de uma interface de caso de uso,
 * não de um serviço de infraestrutura diretamente.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmarPagamentoPorWebhookUseCaseImpl implements ConfirmarPagamentoPorWebhookUseCase {

    private final WebhookProcessingService webhookProcessingService;

    @Override
    public void execute(WebhookPagamentoCommand command) {
        log.debug("[USE_CASE] Delegando ao WebhookProcessingService — paymentId={}, notificationId={}",
                command.paymentId(), command.notificationId());
        webhookProcessingService.processar(command);
    }
}
