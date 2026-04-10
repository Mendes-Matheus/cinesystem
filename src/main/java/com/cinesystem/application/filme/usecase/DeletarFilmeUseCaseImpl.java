package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.port.out.CachePort;
import com.cinesystem.domain.filme.Filme;
import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.filme.FilmeRepository;
import com.cinesystem.domain.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeletarFilmeUseCaseImpl implements DeletarFilmeUseCase {

    private final FilmeRepository repository;
    private final CachePort cachePort;

    public DeletarFilmeUseCaseImpl(FilmeRepository repository, CachePort cachePort) {
        this.repository = repository;
        this.cachePort = cachePort;
    }

    @Override
    @Transactional
    public void execute(FilmeId id) {
        Filme filme = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado"));
        filme.desativar();
        repository.save(filme);
        cachePort.evictByPrefix("filmes:listagem:");
    }
}
