package com.cinesystem.domain.ingresso;

import com.cinesystem.domain.shared.DomainException;
import com.cinesystem.domain.usuario.UsuarioId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IngressoTest {

    @Test
    @DisplayName("Deve criar ingresso com dados válidos")
    void deveCriarIngressoComDadosValidos() {
        var ingresso = new Ingresso(
                new IngressoId(1L),
                new CodigoIngresso("XYZ123"),
                new UsuarioId(10L),
                100L,
                new BigDecimal("25.00"),
                StatusIngresso.ATIVO,
                LocalDateTime.now()
        );

        assertThat(ingresso.getId().id()).isEqualTo(1L);
        assertThat(ingresso.getCodigo().valor()).isEqualTo("XYZ123");
        assertThat(ingresso.getStatus()).isEqualTo(StatusIngresso.ATIVO);
        assertThat(ingresso.getValorPago()).isEqualTo(new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("Deve gerar código UUID automaticamente quando não fornecido")
    void deveGerarCodigoUUIDAutomaticamente_QuandoNaoFornecido() {
        var ingresso = new Ingresso(
                new IngressoId(1L),
                null,
                new UsuarioId(10L),
                100L,
                new BigDecimal("25.00"),
                StatusIngresso.ATIVO,
                LocalDateTime.now()
        );

        assertThat(ingresso.getCodigo()).isNotNull();
        assertThat(ingresso.getCodigo().valor()).isNotEmpty();
    }

    @Test
    @DisplayName("Deve cancelar ingresso ativo")
    void deveCancelarIngressoAtivo() {
        var ingresso = new Ingresso(
                new IngressoId(1L),
                null,
                new UsuarioId(10L),
                100L,
                new BigDecimal("25.00"),
                StatusIngresso.ATIVO,
                LocalDateTime.now()
        );

        ingresso.cancelar();

        assertThat(ingresso.getStatus()).isEqualTo(StatusIngresso.CANCELADO);
    }

    @Test
    @DisplayName("Deve lançar exceção quando cancelar ingresso já cancelado")
    void deveLancarExcecao_QuandoCancelarIngressoJaCancelado() {
        var ingresso = new Ingresso(
                new IngressoId(1L),
                null,
                new UsuarioId(10L),
                100L,
                new BigDecimal("25.00"),
                StatusIngresso.CANCELADO,
                LocalDateTime.now()
        );

        assertThatThrownBy(ingresso::cancelar)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Ingresso não pode ser cancelado");
    }

    @Test
    @DisplayName("Deve lançar exceção quando cancelar ingresso utilizado")
    void deveLancarExcecao_QuandoCancelarIngressoUtilizado() {
        var ingresso = new Ingresso(
                new IngressoId(1L),
                null,
                new UsuarioId(10L),
                100L,
                new BigDecimal("25.00"),
                StatusIngresso.UTILIZADO,
                LocalDateTime.now()
        );

        assertThatThrownBy(ingresso::cancelar)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Ingresso não pode ser cancelado");
    }

    @Test
    @DisplayName("Deve marcar ingresso como utilizado")
    void deveMarcarIngressoComoUtilizado() {
        var ingresso = new Ingresso(
                new IngressoId(1L),
                null,
                new UsuarioId(10L),
                100L,
                new BigDecimal("25.00"),
                StatusIngresso.ATIVO,
                LocalDateTime.now()
        );

        ingresso.marcarUtilizado();

        assertThat(ingresso.getStatus()).isEqualTo(StatusIngresso.UTILIZADO);
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor pago for zero ou negativo")
    void deveLancarExcecao_QuandoValorPagoZeroOuNegativo() {
        assertThatThrownBy(() -> new Ingresso(
                new IngressoId(1L),
                null,
                new UsuarioId(10L),
                100L,
                BigDecimal.ZERO,
                StatusIngresso.ATIVO,
                LocalDateTime.now()
        ))
        .isInstanceOf(DomainException.class)
        .hasMessageContaining("Valor do ingresso deve ser positivo");

        assertThatThrownBy(() -> new Ingresso(
                new IngressoId(1L),
                null,
                new UsuarioId(10L),
                100L,
                new BigDecimal("-5.00"),
                StatusIngresso.ATIVO,
                LocalDateTime.now()
        ))
        .isInstanceOf(DomainException.class)
        .hasMessageContaining("Valor do ingresso deve ser positivo");
    }
}
