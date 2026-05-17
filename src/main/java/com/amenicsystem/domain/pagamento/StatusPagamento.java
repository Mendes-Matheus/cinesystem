package com.amenicsystem.domain.pagamento;

import com.amenicsystem.domain.shared.InvalidPaymentTransitionException;

import java.util.Set;

/**
 * Status do ciclo de vida de um Pagamento no domínio do CineSystem.
 *
 * <h3>Máquina de Estados (FSM Declarativa):</h3>
 * <p>Cada constante carrega o conjunto de transições válidas a partir daquele estado.
 * Transições inválidas lançam {@link InvalidPaymentTransitionException} com contexto
 * detalhado, impedindo regressão de estado ou progressão ilógica.</p>
 *
 * <h3>Transições baseadas no ciclo real do Mercado Pago:</h3>
 * <pre>
 *   PENDENTE    → APROVADO, REJEITADO, CANCELADO, EM_PROCESSO, AUTORIZADO, EM_MEDIACAO
 *   AUTORIZADO  → APROVADO, CANCELADO
 *   EM_PROCESSO → APROVADO, REJEITADO, CANCELADO, EM_MEDIACAO
 *   EM_MEDIACAO → APROVADO, REJEITADO, CANCELADO
 *   APROVADO    → REEMBOLSADO, CONTESTADO
 *   REJEITADO   → (terminal)
 *   CANCELADO   → (terminal)
 *   REEMBOLSADO → (terminal — refunds não se revertem)
 *   CONTESTADO  → (terminal — chargebacks são definitivos)
 *   DESCONHECIDO→ (terminal — sem ação)
 * </pre>
 *
 * <h3>Design:</h3>
 * <p>O campo {@code transicoesValidas} usa {@code Set<String>} com os nomes dos
 * status de destino permitidos. O método {@link #validarTransicao} resolve a comparação
 * por nome, evitando problemas de forward reference em enum (constantes referenciadas
 * antes de serem inicializadas).</p>
 */
public enum StatusPagamento {

    PENDENTE(Set.of("APROVADO", "REJEITADO", "CANCELADO", "EM_PROCESSO", "AUTORIZADO", "EM_MEDIACAO")),
    AUTORIZADO(Set.of("APROVADO", "CANCELADO")),
    EM_PROCESSO(Set.of("APROVADO", "REJEITADO", "CANCELADO", "EM_MEDIACAO")),
    EM_MEDIACAO(Set.of("APROVADO", "REJEITADO", "CANCELADO")),
    APROVADO(Set.of("REEMBOLSADO", "CONTESTADO")),

    // Estados terminais — sem transições válidas
    REJEITADO(Set.of()),
    CANCELADO(Set.of()),
    REEMBOLSADO(Set.of()),
    CONTESTADO(Set.of()),
    DESCONHECIDO(Set.of()); // Status não mapeado — nunca alterar o Ingresso com este valor

    private final Set<String> transicoesValidas;

    StatusPagamento(Set<String> transicoesValidas) {
        this.transicoesValidas = transicoesValidas;
    }

    /**
     * Verifica se a transição para o novo status é válida.
     *
     * @param novoStatus status de destino
     * @return true se a transição for permitida
     */
    public boolean podeTransicionarPara(StatusPagamento novoStatus) {
        return transicoesValidas.contains(novoStatus.name());
    }

    /**
     * Valida a transição ou lança exceção de domínio com contexto detalhado.
     *
     * @param novoStatus status de destino
     * @throws InvalidPaymentTransitionException se a transição não for permitida
     */
    public void validarTransicao(StatusPagamento novoStatus) {
        if (!podeTransicionarPara(novoStatus)) {
            throw new InvalidPaymentTransitionException(this, novoStatus);
        }
    }

    /**
     * Indica se este status é terminal (sem transições possíveis).
     *
     * @return true se o estado é final e não admite mais transições
     */
    public boolean isTerminal() {
        return transicoesValidas.isEmpty();
    }
}
