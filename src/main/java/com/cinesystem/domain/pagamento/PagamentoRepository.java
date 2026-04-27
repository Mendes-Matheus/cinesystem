package com.cinesystem.domain.pagamento;

import java.util.Optional;

public interface PagamentoRepository {
    Pagamento save(Pagamento pagamento);
    Optional<Pagamento> findByIngressoId(Long ingressoId);

    Optional<Pagamento> findByTransacaoId(String transacaoId);
}
