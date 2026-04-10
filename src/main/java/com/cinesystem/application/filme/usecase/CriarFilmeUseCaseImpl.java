package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.CriarFilmeCommand;
import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.application.filme.event.FilmeCriadoEvent;
import com.cinesystem.application.port.out.CachePort;
import com.cinesystem.domain.filme.Filme;
import com.cinesystem.domain.filme.FilmeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarFilmeUseCaseImpl implements CriarFilmeUseCase {

    private final FilmeRepository filmeRepository;
    private final CachePort cachePort;
    private final ApplicationEventPublisher eventPublisher;

    public CriarFilmeUseCaseImpl(FilmeRepository filmeRepository, CachePort cachePort, ApplicationEventPublisher eventPublisher) {
        this.filmeRepository = filmeRepository;
        this.cachePort = cachePort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public FilmeResult execute(CriarFilmeCommand command) {
        Filme filme = new Filme(
                null,
                command.titulo(),
                null,
                command.genero(),
                command.classificacao(),
                command.duracaoMinutos(),
                command.posterUrl(),
                command.dataLancamento(),
                true
        );

        Filme salvo = filmeRepository.save(filme);
        
        cachePort.evictByPrefix("filmes:listagem:");
        eventPublisher.publishEvent(new FilmeCriadoEvent(salvo));
        
        return FilmeResult.from(salvo);
    }
}
