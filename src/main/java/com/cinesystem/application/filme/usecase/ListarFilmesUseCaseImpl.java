package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.application.port.out.CachePort;
import com.cinesystem.application.port.out.query.FilmeQueryPort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class ListarFilmesUseCaseImpl implements ListarFilmesUseCase {

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

        return cachePort.<List<FilmeResult>>get(cacheKey)
                .orElseGet(() -> {
                    List<FilmeResult> result = filmeQueryPort.findAllAtivos(genero);
                    cachePort.set(cacheKey, result, Duration.ofMinutes(15));
                    return result;
                });
    }}
