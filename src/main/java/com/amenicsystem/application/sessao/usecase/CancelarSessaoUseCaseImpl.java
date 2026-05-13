package com.amenicsystem.application.sessao.usecase;

import com.amenicsystem.application.port.out.CachePort;
import com.amenicsystem.domain.sessao.Sessao;
import com.amenicsystem.domain.sessao.SessaoId;
import com.amenicsystem.domain.sessao.SessaoRepository;
import com.amenicsystem.domain.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelarSessaoUseCaseImpl implements CancelarSessaoUseCase {

    private final SessaoRepository sessaoRepository;
    private final CachePort cachePort;

    public CancelarSessaoUseCaseImpl(SessaoRepository sessaoRepository, CachePort cachePort) {
        this.sessaoRepository = sessaoRepository;
        this.cachePort = cachePort;
    }

    @Override
    @Transactional
    public void execute(SessaoId sessaoId) {
        Sessao sessao = sessaoRepository.findById(sessaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada"));

        sessao.cancelar();
        sessaoRepository.save(sessao);
        
        String cacheKey = "sessoes:filme:" + sessao.getFilmeId().id();
        cachePort.evictByPrefix(cacheKey);
    }
}
