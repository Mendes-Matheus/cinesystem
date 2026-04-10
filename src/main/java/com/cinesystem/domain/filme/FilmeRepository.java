package com.cinesystem.domain.filme;

import java.util.Optional;

public interface FilmeRepository {
    Filme save(Filme filme);
    Optional<Filme> findById(FilmeId id);
    void delete(FilmeId id);
}
