package com.cinesystem.infrastructure.persistence.pagamento;

import com.cinesystem.domain.pagamento.MetodoPagamento;
import com.cinesystem.domain.pagamento.StatusPagamento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento")
@Getter
@Setter
@NoArgsConstructor
public class PagamentoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingresso_id", nullable = false)
    private Long ingressoId;

    @Column(name = "transacao_id")
    private String transacaoId;

    @Column(nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPagamento metodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    @Column(name = "processado_em")
    private LocalDateTime processadoEm;
}