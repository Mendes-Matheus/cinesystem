package com.cinesystem.infrastructure.persistence.pagamento;

import com.cinesystem.domain.pagamento.Pagamento;
import com.cinesystem.domain.pagamento.PagamentoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PagamentoRepositoryAdapter implements PagamentoRepository {

    private final PagamentoJpaRepository jpaRepository;
    private final PagamentoJpaMapper mapper;

    public PagamentoRepositoryAdapter(PagamentoJpaRepository jpaRepository, PagamentoJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Pagamento save(Pagamento pagamento) {
        PagamentoJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(pagamento));
        return mapper.toDomainEntity(saved);
    }

    @Override
    public Optional<Pagamento> findByIngressoId(Long ingressoId) {
        return jpaRepository.findByIngressoId(ingressoId)
                .map(mapper::toDomainEntity);
    }
}