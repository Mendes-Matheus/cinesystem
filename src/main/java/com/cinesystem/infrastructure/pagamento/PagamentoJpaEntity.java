package com.cinesystem.infrastructure.pagamento;

import com.cinesystem.domain.pagamento.StatusPagamento;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pagamentos")
public class PagamentoJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingresso_id")
    private Long ingressoId;

    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private StatusPagamento status;

    public PagamentoJpaEntity(Long id, Long ingressoId, BigDecimal valor, StatusPagamento status) {
        this.id = id;
        this.ingressoId = ingressoId;
        this.valor = valor;
        this.status = status;
    }

    public PagamentoJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIngressoId() {
        return ingressoId;
    }

    public void setIngressoId(Long ingressoId) {
        this.ingressoId = ingressoId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }

}
