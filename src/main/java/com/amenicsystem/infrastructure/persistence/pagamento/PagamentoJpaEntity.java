package com.amenicsystem.infrastructure.persistence.pagamento;

import com.amenicsystem.domain.pagamento.MetodoPagamento;
import com.amenicsystem.domain.pagamento.StatusPagamento;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingresso_id", nullable = false)
    private Long ingressoId;

    /**
     * ID da Preference do MP (preferenceId) criada no checkout.
     * Diferente de paymentId — são IDs distintos do MP.
     */
    @Column(name = "transacao_externa_id")
    private String transacaoExternaId;

    /**
     * ID do pagamento efetivado (numérico) — recebido no webhook.
     * Null até a primeira notificação de pagamento.
     */
    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    @Column(name = "metodo")
    @Enumerated(EnumType.STRING)
    private MetodoPagamento metodo;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusPagamento status;

    @Column(name = "dados_retorno", columnDefinition = "TEXT")
    private String dadosRetorno;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "processado_em")
    private LocalDateTime processadoEm;

    /** Suporte a optimistic locking futuro e auditoria de concorrência. */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /** Timestamp do último evento do MP processado — para monotonicidade. */
    @Column(name = "mp_ultima_atualizacao")
    private LocalDateTime mpUltimaAtualizacao;
}