package com.cinesystem.application.auth.usecase;

import com.cinesystem.application.auth.dto.LoginCommand;
import com.cinesystem.application.auth.dto.TokenResult;
import com.cinesystem.application.port.out.JwtPort;
import com.cinesystem.domain.shared.DomainException;
import com.cinesystem.domain.usuario.Email;
import com.cinesystem.domain.usuario.Usuario;
import com.cinesystem.domain.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticarUsuarioUseCaseImpl implements AutenticarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtPort jwtPort;

    public AutenticarUsuarioUseCaseImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtPort jwtPort) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtPort = jwtPort;
    }

    @Override
    public TokenResult execute(LoginCommand command) {
        Usuario usuario = usuarioRepository.findByEmail(new Email(command.email()))
                .orElseThrow(() -> new DomainException("Credenciais inválidas"));

        if (!usuario.getSenha().matches(command.senha(), passwordEncoder)) {
            throw new DomainException("Credenciais inválidas");
        }

        if (!usuario.isAtivo()) {
            throw new DomainException("Conta desativada");
        }

        return jwtPort.gerar(usuario);
    }
}
