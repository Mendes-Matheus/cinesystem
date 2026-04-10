package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.AtualizarFilmeCommand;
import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.application.port.out.CachePort;
import com.cinesystem.domain.filme.Filme;
import com.cinesystem.domain.filme.FilmeRepository;
import com.cinesystem.domain.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtualizarFilmeUseCaseImpl implements AtualizarFilmeUseCase {

    private final FilmeRepository filmeRepository;
    private final CachePort cachePort;

    public AtualizarFilmeUseCaseImpl(FilmeRepository filmeRepository, CachePort cachePort) {
        this.filmeRepository = filmeRepository;
        this.cachePort = cachePort;
    }

    @Override
    @Transactional
    public FilmeResult execute(AtualizarFilmeCommand command) {
        Filme existing = filmeRepository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado: " + command.id().valor()));

        Filme updated = new Filme(
                existing.getId(),
                command.titulo(),
                existing.getSinopse(),
                command.genero(),
                command.classificacao(),
                command.duracaoMinutos(),
                command.posterUrl(),
                existing.getDataLancamento(),
                existing.isAtivo()
        );

        Filme salvo = filmeRepository.save(updated);
        
        cachePort.evictByPrefix("filmes:listagem:");
        
        return FilmeResult.from(salvo);
    }
}
