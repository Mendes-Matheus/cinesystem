package com.cinesystem.domain.usuario;

import com.cinesystem.domain.shared.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    @DisplayName("Deve aceitar e-mail válido")
    void deveAceitarEmailValido() {
        var email = new Email("teste@dominio.com.br");
        assertThat(email.valor()).isEqualTo("teste@dominio.com.br");
    }

    @Test
    @DisplayName("Deve rejeitar e-mail sem arroba")
    void deveRejeitarEmailSemArroba() {
        assertThatThrownBy(() -> new Email("testedominio.com"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("E-mail inválido");
    }

    @Test
    @DisplayName("Deve rejeitar e-mail sem domínio")
    void deveRejeitarEmailSemDominio() {
        assertThatThrownBy(() -> new Email("teste@"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("E-mail inválido");
    }

    @Test
    @DisplayName("Deve rejeitar e-mail nulo")
    void deveRejeitarEmailNulo() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("E-mail inválido");
    }
}
