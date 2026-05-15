package com.amenicsystem.infrastructure.persistence.pagamento;

import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.pagamento.Pagamento;
import com.amenicsystem.domain.pagamento.PagamentoId;
import com.amenicsystem.domain.pagamento.PagamentoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PagamentoRepositoryAdapter implements PagamentoRepository {

    private final PagamentoJpaRepository repository;
    private final PagamentoJpaMapper mapper;

    public PagamentoRepositoryAdapter(PagamentoJpaRepository repository, PagamentoJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Pagamento save(Pagamento pagamento) {
        PagamentoJpaEntity entity = mapper.toJpaEntity(pagamento);
        PagamentoJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Pagamento> findById(PagamentoId id) {
        return repository.findById(id.id()).map(mapper::toDomainEntity);
    }

    @Override
    public Optional<Pagamento> findByTransacaoExternaId(String transacaoExternaId) {
        return repository.findByTransacaoExternaId(transacaoExternaId).map(mapper::toDomainEntity);
    }

    @Override
    public Optional<Pagamento> findByIngressoId(IngressoId ingressoId) {
        return repository.findByIngressoId(ingressoId.id()).map(mapper::toDomainEntity);
    }
}