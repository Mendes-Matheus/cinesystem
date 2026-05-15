package com.amenicsystem.domain.pagamento;

import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.shared.DomainException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagamento {
    private PagamentoId id;
    private IngressoId ingressoId;
    private String transacaoExternaId;
    private BigDecimal valor;
    private MetodoPagamento metodo;
    private StatusPagamento status;
    private String dadosRetorno;
    private LocalDateTime criadoEm;
    private LocalDateTime processadoEm;

    public void aprovar() {
        if (this.status != StatusPagamento.PENDENTE) {
            throw new DomainException("Apenas pagamentos PENDENTEs podem ser aprovados");
        }
        this.status = StatusPagamento.APROVADO;
        this.processadoEm = LocalDateTime.now();
    }

    public void rejeitar() {
        if (this.status != StatusPagamento.PENDENTE) {
            throw new DomainException("Apenas pagamentos PENDENTEs podem ser rejeitados");
        }
        this.status = StatusPagamento.REJEITADO;
        this.processadoEm = LocalDateTime.now();
    }

    public void estornar() {
        if (this.status != StatusPagamento.APROVADO) {
            throw new DomainException("Apenas pagamentos APROVADOS podem ser estornados");
        }
        this.status = StatusPagamento.REEMBOLSADO;
        this.processadoEm = LocalDateTime.now();
    }

    public void cancelar() {
        if (this.status == StatusPagamento.APROVADO || this.status == StatusPagamento.REEMBOLSADO) {
            throw new DomainException("Não é possível cancelar pagamento com status: " + this.status);
        }
        this.status = StatusPagamento.CANCELADO;
        this.processadoEm = LocalDateTime.now();
    }
}
