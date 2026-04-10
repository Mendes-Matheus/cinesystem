package com.cinesystem.domain.ingresso;

import java.util.Optional;

public interface IngressoRepository {
    Ingresso save(Ingresso ingresso);
    Optional<Ingresso> findById(IngressoId id);
    Optional<Ingresso> findByCodigo(CodigoIngresso codigo);
}
