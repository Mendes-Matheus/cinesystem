package com.amenicsystem.domain.assento;

import com.amenicsystem.domain.sala.SalaId;

import java.util.List;
import java.util.Optional;

public interface AssentoRepository {
    List<Assento> findBySala(SalaId salaId);
    Optional<Assento> findById(AssentoId id);
    Assento save(Assento assento);
}
