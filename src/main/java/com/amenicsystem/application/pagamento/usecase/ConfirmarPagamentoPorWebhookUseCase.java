package com.amenicsystem.application.pagamento.usecase;

import com.amenicsystem.application.pagamento.dto.WebhookPagamentoCommand;

public interface ConfirmarPagamentoPorWebhookUseCase {
    void execute(WebhookPagamentoCommand command);
}
