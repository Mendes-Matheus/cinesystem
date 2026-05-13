package com.amenicsystem.application.sessao.usecase;

import com.amenicsystem.application.sessao.dto.AssentoResult;
import com.amenicsystem.application.port.out.CachePort;
import com.amenicsystem.application.port.out.query.SessaoQueryPort;
import com.amenicsystem.domain.sessao.SessaoId;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class BuscarAssentosUseCaseImpl implements BuscarAssentosUseCase {

    private static final TypeReference<List<AssentoResult>> ASSENTO_LIST_TYPE = new TypeReference<>() {};

    private final SessaoQueryPort sessaoQueryPort;
    private final CachePort cachePort;

    public BuscarAssentosUseCaseImpl(SessaoQueryPort sessaoQueryPort, CachePort cachePort) {
        this.sessaoQueryPort = sessaoQueryPort;
        this.cachePort = cachePort;
    }

    @Override
    public List<AssentoResult> execute(SessaoId sessaoId) {
        String cacheKey = "assentos:sessao:" + sessaoId.id();

        return cachePort.get(cacheKey, ASSENTO_LIST_TYPE).orElseGet(() -> {
            List<AssentoResult> result = sessaoQueryPort.findAssentosBySessao(sessaoId);
            cachePort.set(cacheKey, result, Duration.ofSeconds(30));
            return result;
        });
    }
}