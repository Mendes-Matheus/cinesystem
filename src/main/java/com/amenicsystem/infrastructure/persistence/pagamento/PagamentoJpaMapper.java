package com.amenicsystem.infrastructure.persistence.pagamento;

import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.pagamento.Pagamento;
import com.amenicsystem.domain.pagamento.PagamentoId;
import org.springframework.stereotype.Component;

@Component
public class PagamentoJpaMapper {

    public Pagamento toDomainEntity(PagamentoJpaEntity entity) {
        if (entity == null) return null;

        return Pagamento.builder()
                .id(entity.getId() != null ? new PagamentoId(entity.getId()) : null)
                .ingressoId(new IngressoId(entity.getIngressoId()))
                .transacaoExternaId(entity.getTransacaoExternaId())
                .valor(entity.getValor())
                .metodo(entity.getMetodo())
                .status(entity.getStatus())
                .dadosRetorno(entity.getDadosRetorno())
                .criadoEm(entity.getCriadoEm())
                .processadoEm(entity.getProcessadoEm())
                .build();
    }

    public PagamentoJpaEntity toJpaEntity(Pagamento pagamento) {
        if (pagamento == null) return null;

        return PagamentoJpaEntity.builder()
                .id(pagamento.getId() != null ? pagamento.getId().id() : null)
                .ingressoId(pagamento.getIngressoId().id())
                .transacaoExternaId(pagamento.getTransacaoExternaId())
                .valor(pagamento.getValor())
                .metodo(pagamento.getMetodo())
                .status(pagamento.getStatus())
                .dadosRetorno(pagamento.getDadosRetorno())
                .criadoEm(pagamento.getCriadoEm())
                .processadoEm(pagamento.getProcessadoEm())
                .build();
    }
}