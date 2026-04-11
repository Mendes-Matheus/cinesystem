package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.CriarFilmeCommand;
import com.cinesystem.application.port.out.CachePort;
import com.cinesystem.domain.filme.FilmeRepository;
import com.cinesystem.application.filme.event.FilmeCriadoEvent;
import com.cinesystem.domain.filme.ClassificacaoEtaria;
import com.cinesystem.domain.filme.Genero;
import com.cinesystem.domain.shared.DomainException;
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
            var arg = (com.cinesystem.domain.filme.Filme) invocation.getArgument(0);
            return new com.cinesystem.domain.filme.Filme(
                  new com.cinesystem.domain.filme.FilmeId(1L), 
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
