package com.amenicsystem.domain.pagamento;

import com.amenicsystem.domain.ingresso.IngressoId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Port de saída para persistência do aggregate {@link Pagamento}.
 * Implementado pela camada de infraestrutura (JPA adapter).
 */
public interface PagamentoRepository {

    Pagamento save(Pagamento pagamento);

    Optional<Pagamento> findById(PagamentoId id);

    Optional<Pagamento> findByTransacaoExternaId(String transacaoExternaId);

    Optional<Pagamento> findByIngressoId(IngressoId ingressoId);

    /**
     * Busca por ingressoId com lock exclusivo — usado no fallback de correlação
     * para pagamentos antigos sem payment_id. Garante serialização mesmo no path legado.
     */
    Optional<Pagamento> findByIngressoIdWithLock(IngressoId ingressoId);

    /**
     * Busca o Pagamento pelo paymentId do MP com lock exclusivo (SELECT FOR UPDATE).
     * <p>Serializa webhooks concorrentes para o mesmo pagamento.
     * Timeout de 3 segundos configurado no adapter JPA.</p>
     *
     * @param paymentId ID do pagamento no MP
     * @return Pagamento bloqueado ou empty se não encontrado
     */
    Optional<Pagamento> findByPaymentIdWithLock(String paymentId);

    /**
     * Busca pelo preferenceId com lock — fallback para pagamentos
     * antigos que ainda não têm payment_id preenchido.
     *
     * @param transacaoExternaId preferenceId do MP
     * @return Pagamento bloqueado ou empty se não encontrado
     */
    Optional<Pagamento> findByTransacaoExternaIdWithLock(String transacaoExternaId);

    /**
     * Vincula o paymentId ao Pagamento de forma idempotente via UPDATE direto.
     * Executa {@code UPDATE SET payment_id WHERE id = ? AND payment_id IS NULL}.
     *
     * <p><strong>Uso:</strong> NÃO é chamado no fluxo principal de webhook.
     * No webhook, a vinculação ocorre via {@code pagamento.vincularPaymentId()} (método
     * de domínio) persistida pelo {@code save()} subsequente — mantendo o estado em
     * memória sincronizado com o banco.</p>
     *
     * <p>Este método é útil para operações fora do fluxo principal:
     * reconciliation job bulk, scripts de migração, operações administrativas.</p>
     *
     * @return 1 se vinculado agora, 0 se já estava preenchido
     */
    int vincularPaymentId(PagamentoId pagamentoId, String paymentId);

    /**
     * Busca pagamentos PENDENTES criados antes de um threshold de tempo.
     * Usado pelo job de reconciliação para detectar pagamentos "esquecidos".
     *
     * @param criadoAntesDe timestamp limite
     * @return lista de pagamentos pendentes antigos
     */
    List<Pagamento> findPendentesAntigos(LocalDateTime criadoAntesDe);
}
