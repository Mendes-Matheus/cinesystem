package com.cinesystem.domain.filme;

import com.cinesystem.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilmeTest {

    @Test
    void deveCriarFilmeComDadosValidos() {
        // arrange & act
        Filme filme = new Filme(
                new FilmeId(1L),
                "Duna",
                "Ficção científica",
                Genero.FICCAO,
                ClassificacaoEtaria.of("14"),
                156,
                "url",
                LocalDate.now(),
                true
        );

        // assert
        assertThat(filme.getTitulo()).isEqualTo("Duna");
        assertThat(filme.getDuracaoMinutos()).isEqualTo(156);
        assertThat(filme.isAtivo()).isTrue();
    }

    @Test
    void deveRejeitarTituloVazio() {
        // arrange & act & assert
        assertThatThrownBy(() -> new Filme(
                new FilmeId(1L),
                "",
                "Ficção científica",
                Genero.FICCAO,
                ClassificacaoEtaria.of("14"),
                156,
                "url",
                LocalDate.now(),
                true
        )).isInstanceOf(DomainException.class)
          .hasMessageContaining("Título obrigatório");
    }

    @Test
    void deveRejeitarDuracaoZero() {
        // arrange & act & assert
        assertThatThrownBy(() -> new Filme(
                new FilmeId(1L),
                "Duna",
                "Ficção",
                Genero.FICCAO,
                ClassificacaoEtaria.of("14"),
                0,
                "url",
                LocalDate.now(),
                true
        )).isInstanceOf(DomainException.class)
          .hasMessageContaining("Duração deve ser maior que zero");
    }

    @Test
    void deveDesativarFilmeAtivo() {
        // arrange
        Filme filme = new Filme(
                new FilmeId(1L),
                "Duna",
                "Ficção",
                Genero.FICCAO,
                ClassificacaoEtaria.of("14"),
                156,
                "url",
                LocalDate.now(),
                true
        );

        // act
        filme.desativar();

        // assert
        assertThat(filme.isAtivo()).isFalse();
    }

    @Test
    void deveLancarExcecaoAoDesativarFilmeInativo() {
        // arrange
        Filme filme = new Filme(
                new FilmeId(1L),
                "Duna",
                "Ficção",
                Genero.FICCAO,
                ClassificacaoEtaria.of("14"),
                156,
                "url",
                LocalDate.now(),
                false
        );

        // act & assert
        assertThatThrownBy(filme::desativar)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Filme já inativo");
    }
}
