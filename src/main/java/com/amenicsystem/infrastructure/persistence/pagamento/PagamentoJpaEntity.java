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

    @Column(name = "transacao_externa_id")
    private String transacaoExternaId;

    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    @Column(name = "metodo", nullable = false)
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
}