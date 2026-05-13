package com.amenicsystem.application.sessao.usecase;

import com.amenicsystem.application.sessao.dto.SessaoResult;
import com.amenicsystem.application.port.out.CachePort;
import com.amenicsystem.application.port.out.query.SessaoQueryPort;
import com.amenicsystem.domain.filme.FilmeId;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class ListarSessoesPorFilmeUseCaseImpl implements ListarSessoesPorFilmeUseCase {

    private static final TypeReference<List<SessaoResult>> SESSAO_LIST_TYPE = new TypeReference<>() {};

    private final SessaoQueryPort sessaoQueryPort;
    private final CachePort cachePort;

    public ListarSessoesPorFilmeUseCaseImpl(SessaoQueryPort sessaoQueryPort, CachePort cachePort) {
        this.sessaoQueryPort = sessaoQueryPort;
        this.cachePort = cachePort;
    }

    @Override
    public List<SessaoResult> execute(FilmeId filmeId) {
        String cacheKey = "sessoes:filme:" + filmeId.id();

        return cachePort.get(cacheKey, SESSAO_LIST_TYPE).orElseGet(() -> {
            List<SessaoResult> result = sessaoQueryPort.findAtivasByFilme(filmeId);
            cachePort.set(cacheKey, result, Duration.ofMinutes(5));
            return result;
        });
    }
}