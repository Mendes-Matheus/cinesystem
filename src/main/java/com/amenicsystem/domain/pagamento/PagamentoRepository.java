package com.amenicsystem.domain.pagamento;

import com.amenicsystem.domain.ingresso.IngressoId;
import java.util.Optional;

public interface PagamentoRepository {
    Pagamento save(Pagamento pagamento);
    Optional<Pagamento> findById(PagamentoId id);
    Optional<Pagamento> findByTransacaoExternaId(String transacaoExternaId);
    Optional<Pagamento> findByIngressoId(IngressoId ingressoId);
}
