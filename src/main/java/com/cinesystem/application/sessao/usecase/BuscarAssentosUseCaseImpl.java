package com.cinesystem.application.sessao.usecase;

import com.cinesystem.application.sessao.dto.AssentoResult;
import com.cinesystem.application.port.out.CachePort;
import com.cinesystem.application.port.out.query.SessaoQueryPort;
import com.cinesystem.domain.sessao.SessaoId;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class BuscarAssentosUseCaseImpl implements BuscarAssentosUseCase {

    private final SessaoQueryPort sessaoQueryPort;
    private final CachePort cachePort;

    public BuscarAssentosUseCaseImpl(SessaoQueryPort sessaoQueryPort, CachePort cachePort) {
        this.sessaoQueryPort = sessaoQueryPort;
        this.cachePort = cachePort;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AssentoResult> execute(SessaoId sessaoId) {
        String cacheKey = "assentos:sessao:" + sessaoId.id();

        return (List<AssentoResult>) cachePort.get(cacheKey).orElseGet(() -> {
            List<AssentoResult> result = sessaoQueryPort.findAssentosBySessao(sessaoId);
            cachePort.set(cacheKey, result, Duration.ofSeconds(30));
            return result;
        });
    }
}
