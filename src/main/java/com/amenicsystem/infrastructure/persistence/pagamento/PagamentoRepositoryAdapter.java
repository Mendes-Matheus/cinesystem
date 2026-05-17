package com.amenicsystem.infrastructure.persistence.pagamento;

import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.pagamento.Pagamento;
import com.amenicsystem.domain.pagamento.PagamentoId;
import com.amenicsystem.domain.pagamento.PagamentoRepository;
import com.amenicsystem.domain.pagamento.StatusPagamento;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        return repository.findByTransacaoExternaId(transacaoExternaId)
                .map(mapper::toDomainEntity);
    }

    @Override
    public Optional<Pagamento> findByIngressoId(IngressoId ingressoId) {
        return repository.findByIngressoId(ingressoId.id()).map(mapper::toDomainEntity);
    }

    @Override
    public Optional<Pagamento> findByIngressoIdWithLock(IngressoId ingressoId) {
        return repository.findByIngressoIdWithLock(ingressoId.id()).map(mapper::toDomainEntity);
    }

    @Override
    public Optional<Pagamento> findByPaymentIdWithLock(String paymentId) {
        return repository.findByPaymentId(paymentId).map(mapper::toDomainEntity);
    }

    @Override
    public Optional<Pagamento> findByTransacaoExternaIdWithLock(String transacaoExternaId) {
        return repository.findByTransacaoExternaIdWithLock(transacaoExternaId)
                .map(mapper::toDomainEntity);
    }

    @Override
    public int vincularPaymentId(PagamentoId pagamentoId, String paymentId) {
        return repository.vincularPaymentId(pagamentoId.id(), paymentId);
    }

    @Override
    public List<Pagamento> findPendentesAntigos(LocalDateTime criadoAntesDe) {
        return repository.findByStatusAndCriadoEmBefore(StatusPagamento.PENDENTE, criadoAntesDe)
                .stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }
}