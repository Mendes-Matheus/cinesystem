package com.amenicsystem.infrastructure.persistence.pagamento;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PagamentoJpaRepository extends JpaRepository<PagamentoJpaEntity, Long> {
    Optional<PagamentoJpaEntity> findByTransacaoExternaId(String transacaoExternaId);
    Optional<PagamentoJpaEntity> findByIngressoId(Long ingressoId);
}