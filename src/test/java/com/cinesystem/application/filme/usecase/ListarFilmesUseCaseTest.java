package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.application.port.out.CachePort;
import com.cinesystem.application.port.out.query.FilmeQueryPort;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.*;
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
    @DisplayName("Deve retornar do cache quando houver cache hit")
    void deveRetornarDoCache_QuandoCacheHit() {
        var filmesMockados = List.of(
                new FilmeResult(1L, "Mock Filme", "ACAO", "L", 100, null, LocalDate.now())
        );

        when(cachePort.get(
                eq("filmes:listagem:todos"),
                any(TypeReference.class)
        )).thenReturn(Optional.of(filmesMockados));

        var result = useCase.execute(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).titulo()).isEqualTo("Mock Filme");

        verify(filmeQueryPort, never()).findAllAtivos(any());
        verify(cachePort, never()).set(any(), any(), any());
    }

    @Test
    @DisplayName("Deve consultar query port e popular cache quando ocorrer cache miss")
    void deveConsultarQueryPort_EPopularCache_QuandoCacheMiss() {
        var filmesMockados = List.of(
                new FilmeResult(1L, "Mock Filme", "ACAO", "L", 100, null, LocalDate.now())
        );

        when(cachePort.get(
                eq("filmes:listagem:todos"),
                any(TypeReference.class)
        )).thenReturn(Optional.empty());

        when(filmeQueryPort.findAllAtivos(null))
                .thenReturn(filmesMockados);

        var result = useCase.execute(null);

        assertThat(result).hasSize(1);

        verify(filmeQueryPort).findAllAtivos(null);
        verify(cachePort).set(
                "filmes:listagem:todos",
                filmesMockados,
                Duration.ofMinutes(15)
        );
    }

    @Test
    @DisplayName("Deve usar a cache key correta quando gênero for informado")
    void deveUsarCacheKeyCorreta_QuandoGeneroInformado() {
        when(cachePort.get(
                eq("filmes:listagem:acao"),
                any(TypeReference.class)
        )).thenReturn(Optional.empty());

        useCase.execute("ACAO");

        verify(cachePort).get(
                eq("filmes:listagem:acao"),
                any(TypeReference.class)
        );
    }

    @Test
    @DisplayName("Deve usar a cache key correta quando gênero for nulo")
    void deveUsarCacheKeyCorreta_QuandoGeneroNulo() {
        when(cachePort.get(
                eq("filmes:listagem:todos"),
                any(TypeReference.class)
        )).thenReturn(Optional.empty());

        useCase.execute(null);

        verify(cachePort).get(
                eq("filmes:listagem:todos"),
                any(TypeReference.class)
        );
    }
}