package com.cinesystem.infrastructure.persistence.pagamento;

import com.cinesystem.domain.pagamento.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PagamentoJpaRepository extends JpaRepository<PagamentoJpaEntity, Long> {
    Optional<PagamentoJpaEntity> findByIngressoId(Long ingressoId);
    Optional<PagamentoJpaEntity> findByTransacaoId(String transacaoId);
}