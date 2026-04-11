package com.cinesystem.domain.sessao;

import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.sala.SalaId;
import com.cinesystem.domain.shared.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessaoTest {

    @Test
    @DisplayName("Deve criar sessão com dados válidos")
    void deveCriarSessaoComDadosValidos() {
        var sessao = new Sessao(
                new SessaoId(1L),
                new FilmeId(10L),
                new SalaId(5L),
                LocalDateTime.now().plusDays(1),
                "LEGENDADO",
                FormatoExibicao.IMAX,
                new BigDecimal("45.00"),
                StatusSessao.ATIVA
        );

        assertThat(sessao.getId().id()).isEqualTo(1L);
        assertThat(sessao.getStatus()).isEqualTo(StatusSessao.ATIVA);
        assertThat(sessao.getPreco()).isEqualTo(new BigDecimal("45.00"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando data/hora no passado")
    void deveLancarExcecao_QuandoDataHoraNoPassado() {
        assertThatThrownBy(() -> new Sessao(
                new SessaoId(1L),
                new FilmeId(10L),
                new SalaId(5L),
                LocalDateTime.now().minusDays(1),
                "LEGENDADO",
                FormatoExibicao.IMAX,
                new BigDecimal("45.00"),
                StatusSessao.ATIVA
        ))
        .isInstanceOf(DomainException.class)
        .hasMessageContaining("Sessão deve ser agendada no futuro");
    }

    @Test
    @DisplayName("Deve lançar exceção quando preço zero")
    void deveLancarExcecao_QuandoPrecoZero() {
        assertThatThrownBy(() -> new Sessao(
                new SessaoId(1L),
                new FilmeId(10L),
                new SalaId(5L),
                LocalDateTime.now().plusDays(1),
                "LEGENDADO",
                FormatoExibicao.IMAX,
                BigDecimal.ZERO,
                StatusSessao.ATIVA
        ))
        .isInstanceOf(DomainException.class)
        .hasMessageContaining("Preço deve ser maior que zero");
    }

    @Test
    @DisplayName("Deve cancelar sessão ativa")
    void deveCancelarSessaoAtiva() {
        var sessao = new Sessao(
                new SessaoId(1L),
                new FilmeId(10L),
                new SalaId(5L),
                LocalDateTime.now().plusDays(1),
                "LEGENDADO",
                FormatoExibicao.IMAX,
                new BigDecimal("45.00"),
                StatusSessao.ATIVA
        );

        sessao.cancelar();

        assertThat(sessao.getStatus()).isEqualTo(StatusSessao.CANCELADA);
    }

    @Test
    @DisplayName("Deve lançar exceção quando cancelar sessão já cancelada")
    void deveLancarExcecao_QuandoCancelarSessaoJaCancelada() {
        var sessao = new Sessao(
                new SessaoId(1L),
                new FilmeId(10L),
                new SalaId(5L),
                LocalDateTime.now().plusDays(1),
                "LEGENDADO",
                FormatoExibicao.IMAX,
                new BigDecimal("45.00"),
                StatusSessao.CANCELADA
        );

        assertThatThrownBy(sessao::cancelar)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Apenas sessões ATIVAs podem ser canceladas");
    }
}
