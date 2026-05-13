package com.amenicsystem.application.filme.event;

import com.amenicsystem.domain.filme.Filme;
import java.time.LocalDateTime;

public record FilmeCriadoEvent(Filme filme, LocalDateTime occurredOn) {
    public FilmeCriadoEvent(Filme filme) {
        this(filme, LocalDateTime.now());
    }
}
