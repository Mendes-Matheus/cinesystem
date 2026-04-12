package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.application.port.out.CachePort;
import com.cinesystem.application.port.out.query.FilmeQueryPort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

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

        Optional<List<FilmeResult>> cached = cachePort.get(cacheKey)
                .flatMap(value -> toFilmeResults(value, cacheKey));

        if (cached.isPresent()) {
            return cached.get();
        }

        List<FilmeResult> result = filmeQueryPort.findAllAtivos(genero);
        cachePort.set(cacheKey, result, Duration.ofMinutes(15));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Optional<List<FilmeResult>> toFilmeResults(Object value, String cacheKey) {
        if (!(value instanceof List<?> list)) {
            cachePort.evictByPrefix(cacheKey);
            return Optional.empty();
        }

        boolean cacheValido = list.stream().allMatch(FilmeResult.class::isInstance);
        if (!cacheValido) {
            cachePort.evictByPrefix(cacheKey);
            return Optional.empty();
        }

        return Optional.of((List<FilmeResult>) list);
    }
}
