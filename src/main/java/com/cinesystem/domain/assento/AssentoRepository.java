package com.cinesystem.domain.assento;

import com.cinesystem.domain.sala.SalaId;

import java.util.List;
import java.util.Optional;

public interface AssentoRepository {
    List<Assento> findBySala(SalaId salaId);
    Optional<Assento> findById(AssentoId id);
    Assento save(Assento assento);
}
