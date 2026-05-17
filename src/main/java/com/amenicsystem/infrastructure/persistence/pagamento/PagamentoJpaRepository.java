package com.amenicsystem.infrastructure.persistence.pagamento;

import com.amenicsystem.domain.pagamento.StatusPagamento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PagamentoJpaRepository extends JpaRepository<PagamentoJpaEntity, Long> {

    Optional<PagamentoJpaEntity> findByTransacaoExternaId(String transacaoExternaId);

    Optional<PagamentoJpaEntity> findByIngressoId(Long ingressoId);

    /** Busca por ingressoId com lock — fallback para pagamentos sem payment_id preenchido. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT p FROM PagamentoJpaEntity p WHERE p.ingressoId = :ingressoId")
    Optional<PagamentoJpaEntity> findByIngressoIdWithLock(@Param("ingressoId") Long ingressoId);

    /**
     * Busca por payment_id com lock exclusivo (SELECT FOR UPDATE, timeout 3s).
     * Serializa webhooks concorrentes para o mesmo pagamento.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    Optional<PagamentoJpaEntity> findByPaymentId(String paymentId);

    /**
     * Busca por preferenceId com lock — fallback one-time para pagamentos antigos.
     * Requer @Query explícita: Spring Data parseia o sufixo "ForUpdate" como campo de
     * String ao usar derived queries, causando PropertyReferenceException no startup.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT p FROM PagamentoJpaEntity p WHERE p.transacaoExternaId = :transacaoExternaId")
    Optional<PagamentoJpaEntity> findByTransacaoExternaIdWithLock(@Param("transacaoExternaId") String transacaoExternaId);

    /**
     * Vincula payment_id de forma idempotente — só escreve se ainda NULL.
     * Race-safe: dois threads com o mesmo pagamento nunca sobrescreverão um ao outro.
     */
    @Modifying
    @Query("UPDATE PagamentoJpaEntity p SET p.paymentId = :paymentId " +
           "WHERE p.id = :id AND p.paymentId IS NULL")
    int vincularPaymentId(@Param("id") Long id, @Param("paymentId") String paymentId);

    /**
     * Para o reconciliation job: pagamentos PENDENTES criados antes do threshold.
     */
    @Query("SELECT p FROM PagamentoJpaEntity p " +
           "WHERE p.status = :status AND p.criadoEm < :threshold")
    List<PagamentoJpaEntity> findByStatusAndCriadoEmBefore(
            @Param("status") StatusPagamento status,
            @Param("threshold") LocalDateTime threshold);
}