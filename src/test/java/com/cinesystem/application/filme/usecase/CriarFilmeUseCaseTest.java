package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.CriarFilmeCommand;
import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.application.filme.event.FilmeCriadoEvent;
import com.cinesystem.application.port.out.CachePort;
import com.cinesystem.domain.filme.ClassificacaoEtaria;
import com.cinesystem.domain.filme.Filme;
import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.filme.FilmeRepository;
import com.cinesystem.domain.filme.Genero;
import com.cinesystem.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarFilmeUseCaseTest {

    @Mock
    private FilmeRepository filmeRepository;

    @Mock
    private CachePort cachePort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CriarFilmeUseCaseImpl useCase;

    @Test
    void deveCriarFilme_EEvictarCache_EPublicarEvento() {
        // arrange
        CriarFilmeCommand command = new CriarFilmeCommand(
                "Duna",
                Genero.FICCAO,
                ClassificacaoEtaria.of("14"),
                156,
                "url",
                LocalDate.now()
        );

        when(filmeRepository.save(any(Filme.class))).thenAnswer(invocation -> {
            Filme filme = invocation.getArgument(0);
            return new Filme(
                    new FilmeId(1L),
                    filme.getTitulo(),
                    filme.getSinopse(),
                    filme.getGenero(),
                    filme.getClassificacao(),
                    filme.getDuracaoMinutos(),
                    filme.getPosterUrl(),
                    filme.getDataLancamento(),
                    filme.isAtivo()
            );
        });

        // act
        FilmeResult result = useCase.execute(command);

        // assert
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.titulo()).isEqualTo("Duna");

        verify(cachePort).evictByPrefix("filmes:listagem:");
        verify(eventPublisher).publishEvent(any(FilmeCriadoEvent.class));
    }

    @Test
    void deveLancarExcecao_QuandoDominioRejeita() {
        // arrange
        CriarFilmeCommand command = new CriarFilmeCommand(
                "", // Titulo vazio!
                Genero.FICCAO,
                ClassificacaoEtaria.of("14"),
                156,
                "url",
                LocalDate.now()
        );

        // act & assert
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Título obrigatório");

        verifyNoInteractions(filmeRepository, cachePort, eventPublisher);
    }
}
