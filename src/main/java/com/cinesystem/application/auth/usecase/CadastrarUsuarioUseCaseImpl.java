package com.cinesystem.application.auth.usecase;

import com.cinesystem.application.auth.dto.CadastroCommand;
import com.cinesystem.application.auth.dto.TokenResult;
import com.cinesystem.application.port.out.JwtPort;
import com.cinesystem.domain.shared.DomainException;
import com.cinesystem.domain.usuario.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastrarUsuarioUseCaseImpl implements CadastrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtPort jwtPort;

    public CadastrarUsuarioUseCaseImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtPort jwtPort) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtPort = jwtPort;
    }

    @Override
    @Transactional
    public TokenResult execute(CadastroCommand command) {
        Email email = new Email(command.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new DomainException("E-mail já cadastrado");
        }

        Senha senha = Senha.criar(command.senha(), passwordEncoder);
        Usuario usuario = new Usuario(null, command.nome(), email, senha, Role.CLIENTE, true);
        
        Usuario salvo = usuarioRepository.save(usuario);
        return jwtPort.gerar(salvo);
    }
}
