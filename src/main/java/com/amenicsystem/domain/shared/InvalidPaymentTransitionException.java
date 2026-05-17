package com.amenicsystem.domain.shared;

import com.amenicsystem.domain.pagamento.StatusPagamento;

/**
 * Transição de estado inválida na máquina de estados do {@code Pagamento}.
 *
 * <p><strong>Non-retryable:</strong> A FSM do domínio rejeitou a transição
 * {@code statusAtual → novoStatus}. Retentar com o mesmo status resultará
 * no mesmo erro — a transição é logicamente inválida dado o estado atual.</p>
 *
 * <p>Lançada por {@link StatusPagamento#validarTransicao(StatusPagamento)}.</p>
 */
public class InvalidPaymentTransitionException extends WebhookException {

    private final StatusPagamento statusAtual;
    private final StatusPagamento statusSolicitado;

    public InvalidPaymentTransitionException(StatusPagamento statusAtual,
                                              StatusPagamento statusSolicitado) {
        super(String.format("Transição inválida: %s → %s. " +
                        "Transições válidas a partir de %s: %s",
                statusAtual.name(), statusSolicitado.name(),
                statusAtual.name(),
                statusAtual.isTerminal() ? "nenhuma (estado terminal)" : "ver StatusPagamento"),
                false);
        this.statusAtual = statusAtual;
        this.statusSolicitado = statusSolicitado;
    }

    public StatusPagamento getStatusAtual() { return statusAtual; }
    public StatusPagamento getStatusSolicitado() { return statusSolicitado; }
}
