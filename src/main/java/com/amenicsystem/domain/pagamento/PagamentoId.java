package com.amenicsystem.domain.pagamento;

import java.util.Objects;

public record PagamentoId(Long id) {
    public PagamentoId {
        Objects.requireNonNull(id, "PagamentoId não pode ser nulo");
    }
}
