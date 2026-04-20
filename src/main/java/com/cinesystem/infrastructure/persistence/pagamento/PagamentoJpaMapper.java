package com.cinesystem.infrastructure.persistence.pagamento;

import com.cinesystem.domain.pagamento.Pagamento;
import org.springframework.stereotype.Component;

@Component
public class PagamentoJpaMapper {

    public Pagamento toDomainEntity(PagamentoJpaEntity entity) {
        if (entity == null) return null;
        return new Pagamento(
                entity.getId(),
                entity.getIngressoId(),
                entity.getTransacaoId(),
                entity.getValor(),
                entity.getMetodo(),
                entity.getStatus(),
                entity.getProcessadoEm()
        );
    }

    public PagamentoJpaEntity toJpaEntity(Pagamento pagamento) {
        if (pagamento == null) return null;
        PagamentoJpaEntity entity = new PagamentoJpaEntity();
        entity.setId(pagamento.getId());
        entity.setIngressoId(pagamento.getIngressoId());
        entity.setTransacaoId(pagamento.getTransacaoId());
        entity.setValor(pagamento.getValor());
        entity.setMetodo(pagamento.getMetodo());
        entity.setStatus(pagamento.getStatus());
        entity.setProcessadoEm(pagamento.getProcessadoEm());
        return entity;
    }
}