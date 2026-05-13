package com.amenicsystem.application.filme.usecase;

import com.amenicsystem.application.filme.dto.CriarFilmeCommand;
import com.amenicsystem.application.port.out.CachePort;
import com.amenicsystem.domain.filme.FilmeRepository;
import com.amenicsystem.application.filme.event.FilmeCriadoEvent;
import com.amenicsystem.domain.filme.ClassificacaoEtaria;
import com.amenicsystem.domain.filme.Genero;
import com.amenicsystem.domain.shared.DomainException;
import org.junit.jupiter.api.DisplayName;
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
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private CriarFilmeUseCaseImpl useCase;

    @Test
    @DisplayName("Deve criar filme, evictar cache e publicar evento")
    void deveCriarFilme_EEvictarCache_EPublicarEvento() {
        // arrange
        var command = new CriarFilmeCommand("Duna", Genero.FICCAO, new ClassificacaoEtaria("12"), 156, null, LocalDate.now());
        when(filmeRepository.save(any())).thenAnswer(invocation -> {
            var arg = (com.amenicsystem.domain.filme.Filme) invocation.getArgument(0);
            return new com.amenicsystem.domain.filme.Filme(
                  new com.amenicsystem.domain.filme.FilmeId(1L),
                  arg.getTitulo(), arg.getSinopse(), arg.getGenero(), arg.getClassificacao(),
                  arg.getDuracaoMinutos(), arg.getPosterUrl(), arg.getDataLancamento(), arg.isAtivo()
            );
        });

        // act
        var result = useCase.execute(command);

        // assert
        assertThat(result.titulo()).isEqualTo("Duna");
        verify(filmeRepository).save(any());
        verify(cachePort).evictByPrefix("filmes:listagem:");
        verify(publisher).publishEvent(any(FilmeCriadoEvent.class));
    }

    @Test
    @DisplayName("Deve propagar DomainException quando dados inválidos")
    void devePropagar_DomainException_QuandoDadosInvalidos() {
        // arrange
        var command = new CriarFilmeCommand("", Genero.FICCAO, new ClassificacaoEtaria("12"), 156, null, LocalDate.now());

        // act & assert
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class);
        
        verify(filmeRepository, never()).save(any());
        verify(cachePort, never()).evictByPrefix(any());
        verify(publisher, never()).publishEvent(any());
    }
}
