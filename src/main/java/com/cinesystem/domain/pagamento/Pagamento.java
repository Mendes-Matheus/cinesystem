package com.cinesystem.domain.pagamento;

import com.cinesystem.domain.shared.DomainException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pagamento {
    private Long id;
    private Long ingressoId;
    private String transacaoId;
    private BigDecimal valor;
    private MetodoPagamento metodo;
    private StatusPagamento status;
    private LocalDateTime processadoEm;

    public Pagamento(Long id, Long ingressoId, String transacaoId, BigDecimal valor, MetodoPagamento metodo, StatusPagamento status, LocalDateTime processadoEm) {
        this.id = id;
        this.ingressoId = ingressoId;
        this.transacaoId = transacaoId;
        this.valor = valor;
        this.metodo = metodo;
        this.status = status != null ? status : StatusPagamento.PENDENTE;
        this.processadoEm = processadoEm;
    }

    public void aprovar(String transacaoId) {
        if (this.status != StatusPagamento.PENDENTE) {
            throw new DomainException("Apenas pagamentos PENDENTEs podem ser aprovados");
        }
        this.transacaoId = transacaoId;
        this.status = StatusPagamento.APROVADO;
        this.processadoEm = LocalDateTime.now();
    }

    public void estornar() {
        if (this.status != StatusPagamento.APROVADO) {
            throw new DomainException("Apenas pagamentos APROVADOs podem ser estornados");
        }
        this.status = StatusPagamento.ESTORNADO;
    }

    public Long getId() { return id; }
    public Long getIngressoId() { return ingressoId; }
    public String getTransacaoId() { return transacaoId; }
    public BigDecimal getValor() { return valor; }
    public MetodoPagamento getMetodo() { return metodo; }
    public StatusPagamento getStatus() { return status; }
    public LocalDateTime getProcessadoEm() { return processadoEm; }
}
