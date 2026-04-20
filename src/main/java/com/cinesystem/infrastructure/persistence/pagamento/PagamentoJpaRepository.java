package com.cinesystem.infrastructure.persistence.pagamento;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PagamentoJpaRepository extends JpaRepository<PagamentoJpaEntity, Long> {
    Optional<PagamentoJpaEntity> findByIngressoId(Long ingressoId);
}