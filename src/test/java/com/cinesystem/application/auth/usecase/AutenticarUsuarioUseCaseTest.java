package com.cinesystem.application.auth.usecase;

import com.cinesystem.application.auth.dto.LoginCommand;
import com.cinesystem.application.auth.dto.TokenResult;
import com.cinesystem.application.port.out.JwtPort;
import com.cinesystem.domain.shared.DomainException;
import com.cinesystem.domain.usuario.Email;
import com.cinesystem.domain.usuario.Role;
import com.cinesystem.domain.usuario.Senha;
import com.cinesystem.domain.usuario.Usuario;
import com.cinesystem.domain.usuario.UsuarioId;
import com.cinesystem.domain.usuario.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtPort jwtPort;

    @InjectMocks
    private AutenticarUsuarioUseCaseImpl useCase;

    @Test
    @DisplayName("Deve autenticar quando credenciais forem válidas")
    void deveAutenticar_QuandoCredenciaisValidas() {
        // arrange
        var command = new LoginCommand("teste@dominio.com", "senha1234");
        var usuario = new Usuario(new UsuarioId(1L), "Teste", new Email("teste@dominio.com"), Senha.criar("senha1234", passwordEncoder), Role.CLIENTE, true);
        
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha1234", usuario.getSenha().hash())).thenReturn(true);
        when(jwtPort.gerar(any())).thenReturn(new TokenResult("token123", "Bearer", 3600L));

        // act
        var result = useCase.execute(command);

        // assert
        assertThat(result.accessToken()).isEqualTo("token123");
    }

    @Test
    @DisplayName("Deve lançar DomainException quando email não encontrado")
    void deveLancarDomainException_QuandoEmailNaoEncontrado() {
        // arrange
        var command = new LoginCommand("nulo@dominio.com", "senha1234");
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("Deve lançar DomainException quando senha incorreta")
    void deveLancarDomainException_QuandoSenhaIncorreta() {
        // arrange
        var command = new LoginCommand("teste@dominio.com", "senhaErrada");
        var usuario = new Usuario(new UsuarioId(1L), "Teste", new Email("teste@dominio.com"), Senha.criar("senha1234", passwordEncoder), Role.CLIENTE, true);
        
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaErrada", usuario.getSenha().hash())).thenReturn(false);

        // act & assert
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    @DisplayName("Deve lançar DomainException quando conta desativada")
    void deveLancarDomainException_QuandoContaDesativada() {
        // arrange
        var command = new LoginCommand("teste@dominio.com", "senha1234");
        var usuario = new Usuario(new UsuarioId(1L), "Teste", new Email("teste@dominio.com"), Senha.criar("senha1234", passwordEncoder), Role.CLIENTE, true);
        usuario.desativar();
        
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha1234", usuario.getSenha().hash())).thenReturn(true);

        // act & assert
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("Conta desativada");
    }
}
