package com.cinesystem.domain.ingresso;

import com.cinesystem.domain.shared.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CodigoIngressoTest {

    @Test
    @DisplayName("Deve gerar códigos únicos ao chamar gerar duas vezes")
    void deveGerarCodigosUnicos_AoChamarGerarDuasVezes() {
        var codigo1 = CodigoIngresso.gerar();
        var codigo2 = CodigoIngresso.gerar();

        assertThat(codigo1.valor()).isNotEqualTo(codigo2.valor());
    }

    @Test
    @DisplayName("Deve rejeitar código vazio")
    void deveRejeitarCodigoVazio() {
        assertThatThrownBy(() -> new CodigoIngresso("   "))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Código de ingresso inválido");
    }

    @Test
    @DisplayName("Deve rejeitar código nulo")
    void deveRejeitarCodigoNulo() {
        assertThatThrownBy(() -> new CodigoIngresso(null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Código de ingresso inválido");
    }
}
