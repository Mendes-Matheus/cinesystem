package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.application.port.out.CachePort;
import com.cinesystem.application.port.out.query.FilmeQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListarFilmesUseCaseTest {

    @Mock
    private FilmeQueryPort filmeQueryPort;

    @Mock
    private CachePort cachePort;

    @InjectMocks
    private ListarFilmesUseCaseImpl useCase;

    @Test
    void deveRetornarDoCache_QuandoCacheHit() {
        // arrange
        String cacheKey = "filmes:listagem:todos";
        List<FilmeResult> cachedResults = List.of(
                new FilmeResult(1L, "Duna", "FICCAO", "14", 156, "url", LocalDate.now())
        );
        when(cachePort.get(cacheKey)).thenReturn(Optional.of(cachedResults));

        // act
        List<FilmeResult> result = useCase.execute(null);

        // assert
        assertThat(result).hasSize(1).isEqualTo(cachedResults);
        verifyNoInteractions(filmeQueryPort);
    }

    @Test
    void deveConsultarBancoEPopularCache_QuandoCacheMiss() {
        // arrange
        String cacheKey = "filmes:listagem:todos";
        when(cachePort.get(cacheKey)).thenReturn(Optional.empty());
        
        List<FilmeResult> dbResults = List.of(
                new FilmeResult(1L, "Duna", "FICCAO", "14", 156, "url", LocalDate.now())
        );
        when(filmeQueryPort.findAllAtivos(null)).thenReturn(dbResults);

        // act
        List<FilmeResult> result = useCase.execute(null);

        // assert
        assertThat(result).hasSize(1).isEqualTo(dbResults);
        verify(cachePort).set(eq(cacheKey), eq(dbResults), eq(Duration.ofMinutes(15)));
    }

    @Test
    void deveFiltrarPorGenero_QuandoGeneroInformado() {
        // arrange
        String genero = "FICCAO";
        String cacheKey = "filmes:listagem:ficcao"; // toLowerCase
        when(cachePort.get(cacheKey)).thenReturn(Optional.empty());
        
        List<FilmeResult> dbResults = List.of(
                new FilmeResult(1L, "Duna", "FICCAO", "14", 156, "url", LocalDate.now())
        );
        when(filmeQueryPort.findAllAtivos(genero)).thenReturn(dbResults);

        // act
        List<FilmeResult> result = useCase.execute(genero);

        // assert
        assertThat(result).hasSize(1).isEqualTo(dbResults);
        verify(cachePort).set(eq(cacheKey), eq(dbResults), eq(Duration.ofMinutes(15)));
    }
}
