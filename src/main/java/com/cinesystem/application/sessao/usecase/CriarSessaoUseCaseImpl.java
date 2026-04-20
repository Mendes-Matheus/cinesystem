package com.cinesystem.application.sessao.usecase;

import com.cinesystem.application.sessao.dto.CriarSessaoCommand;
import com.cinesystem.application.sessao.dto.SessaoResult;
import com.cinesystem.application.port.out.CachePort;
import com.cinesystem.domain.filme.FilmeRepository;
import com.cinesystem.domain.sessao.Sessao;
import com.cinesystem.domain.sessao.SessaoRepository;
import com.cinesystem.domain.sessao.StatusSessao;
import com.cinesystem.domain.shared.ResourceNotFoundException;
import com.cinesystem.domain.filme.Filme;
import com.cinesystem.domain.assento.AssentoRepository;
import com.cinesystem.domain.assento.Assento;
import com.cinesystem.domain.sessao.SessaoAssento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CriarSessaoUseCaseImpl implements CriarSessaoUseCase {

    private final SessaoRepository sessaoRepository;
    private final FilmeRepository filmeRepository;
    private final AssentoRepository assentoRepository;
    private final CachePort cachePort;

    public CriarSessaoUseCaseImpl(SessaoRepository sessaoRepository, FilmeRepository filmeRepository, AssentoRepository assentoRepository, CachePort cachePort) {
        this.sessaoRepository = sessaoRepository;
        this.filmeRepository = filmeRepository;
        this.assentoRepository = assentoRepository;
        this.cachePort = cachePort;
    }

    @Override
    @Transactional
    public SessaoResult execute(CriarSessaoCommand command) {
        Filme filme = filmeRepository.findById(command.filmeId())
                .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado"));

        Sessao sessao = new Sessao(
                null,
                command.filmeId(),
                command.salaId(),
                command.dataHora(),
                command.idioma(),
                command.formato(),
                command.preco(),
                StatusSessao.ATIVA
        );

        Sessao salva = sessaoRepository.save(sessao);

        Integer mapeados = gerarAssentosDaSala(salva);

        cachePort.evictByPrefix("sessoes:filme:");

        return SessaoResult.from(salva, filme.getTitulo(), "Sala " + command.salaId().id(), mapeados);
    }

    private Integer gerarAssentosDaSala(Sessao sessao) {
        List<Assento> assentosDaSala = assentoRepository.findBySala(sessao.getSalaId());
        List<SessaoAssento> mapeados = assentosDaSala.stream()
                .map(assento -> new SessaoAssento(
                        null,
                        sessao.getId(),
                        assento.getId(),
                        com.cinesystem.domain.assento.StatusAssento.DISPONIVEL,
                        null,
                        null
                ))
        .collect(Collectors.toList());
        sessaoRepository.saveAllAssentos(mapeados);

        return mapeados.size();
    }
}
