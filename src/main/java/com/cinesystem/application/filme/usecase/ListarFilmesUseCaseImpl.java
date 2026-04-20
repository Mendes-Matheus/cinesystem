package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.application.port.out.CachePort;
import com.cinesystem.application.port.out.query.FilmeQueryPort;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class ListarFilmesUseCaseImpl implements ListarFilmesUseCase {

    private static final TypeReference<List<FilmeResult>> FILME_LIST_TYPE = new TypeReference<>() {};

    private final FilmeQueryPort filmeQueryPort;
    private final CachePort cachePort;

    public ListarFilmesUseCaseImpl(FilmeQueryPort filmeQueryPort, CachePort cachePort) {
        this.filmeQueryPort = filmeQueryPort;
        this.cachePort = cachePort;
    }

    @Override
    public List<FilmeResult> execute(String genero) {
        String baseKey = genero != null ? genero.toLowerCase() : "todos";
        String cacheKey = "filmes:listagem:" + baseKey;

        Optional<List<FilmeResult>> cached = cachePort.get(cacheKey, FILME_LIST_TYPE);
        if (cached.isPresent()) {
            return cached.get();
        }

        List<FilmeResult> result = filmeQueryPort.findAllAtivos(genero);
        cachePort.set(cacheKey, result, Duration.ofMinutes(15));
        return result;
    }
}