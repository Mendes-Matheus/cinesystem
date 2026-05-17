package com.amenicsystem.domain.pagamento;

import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.shared.InvalidPaymentTransitionException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Aggregate root do ciclo de vida de um Pagamento.
 *
 * <h3>Máquina de Estados:</h3>
 * <p>A transição de status é controlada pela FSM declarativa em {@link StatusPagamento}.
 * Use {@link #transicionarPara(StatusPagamento)} para qualquer mudança de status —
 * transições inválidas lançam {@link InvalidPaymentTransitionException} sem alterar o estado.</p>
 *
 * <h3>Correlação com Mercado Pago:</h3>
 * <ul>
 *   <li>{@code transacaoExternaId} — ID da Preference (ex: APP_USR-abc123) criada no checkout</li>
 *   <li>{@code paymentId} — ID do pagamento efetivado (ex: "1405334703"), recebido no webhook</li>
 * </ul>
 * <p>São IDs distintos. O {@code paymentId} só é conhecido após o pagamento ser realizado.</p>
 *
 * <h3>Campos de Auditoria e Concorrência:</h3>
 * <ul>
 *   <li>{@code version} — incrementado em cada UPDATE (suporte a @Version JPA)</li>
 *   <li>{@code mpUltimaAtualizacao} — timestamp do último evento processado do MP,
 *       usado para detectar eventos fora de ordem</li>
 * </ul>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagamento {

    private PagamentoId id;
    private IngressoId ingressoId;

    /**
     * ID da Preference criada no Checkout Pro (preferenceId do MP).
     * Diferente do {@code paymentId} (ID do pagamento efetivado).
     */
    private String transacaoExternaId;

    /**
     * ID numérico do pagamento efetivado pelo MP (ex: "1405334703").
     * Recebido no campo {@code data.id} do webhook. Null até a primeira notificação.
     */
    private String paymentId;

    private BigDecimal valor;
    private MetodoPagamento metodo;
    private StatusPagamento status;
    private String dadosRetorno;
    private LocalDateTime criadoEm;
    private LocalDateTime processadoEm;

    /**
     * Versão para controle de concorrência (@Version JPA).
     * Incrementado automaticamente em cada UPDATE pelo JPA.
     */
    private Long version;

    /**
     * Timestamp do último evento do MP processado com sucesso.
     * Usado para detectar webhooks fora de ordem (log WARN, não bloqueio).
     */
    private LocalDateTime mpUltimaAtualizacao;

    // ──────────────────────────────────────────────────────────
    // Comportamento do Aggregate
    // ──────────────────────────────────────────────────────────

    /**
     * Aplica uma transição de estado validada pela FSM declarativa.
     *
     * <p>A FSM em {@link StatusPagamento} define todas as transições válidas.
     * Esta é a ÚNICA forma de alterar o status de um Pagamento —
     * métodos isolados ({@code aprovar()}, {@code rejeitar()}, etc.) foram removidos
     * para forçar o uso da máquina de estados.</p>
     *
     * @param novoStatus status de destino
     * @throws InvalidPaymentTransitionException se a transição não for permitida
     */
    public void transicionarPara(StatusPagamento novoStatus) {
        this.status.validarTransicao(novoStatus);
        this.status = novoStatus;
        this.processadoEm = LocalDateTime.now();
    }

    /**
     * Vincula o paymentId numérico do MP a este Pagamento.
     * Chamado uma única vez quando a primeira notificação é recebida.
     * Idempotente — não sobrescreve se já vinculado.
     *
     * @param mpPaymentId ID do pagamento (ex: "1405334703")
     */
    public void vincularPaymentId(String mpPaymentId) {
        if (this.paymentId == null && mpPaymentId != null && !mpPaymentId.isBlank()) {
            this.paymentId = mpPaymentId;
        }
    }

    /**
     * Atualiza o timestamp do último evento do MP processado.
     * Usado para monitorar ordenação de eventos.
     *
     * @param timestamp timestamp do evento
     */
    public void atualizarTimestampMP(LocalDateTime timestamp) {
        if (timestamp != null) {
            this.mpUltimaAtualizacao = timestamp;
        }
    }

    /**
     * Verifica se o status atual é terminal (sem transições possíveis).
     *
     * @return true se o pagamento está em estado final
     */
    public boolean isTerminal() {
        return this.status != null && this.status.isTerminal();
    }
}
