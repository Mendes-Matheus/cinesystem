package com.amenicsystem.application.auth.usecase;

import com.amenicsystem.application.auth.dto.CadastroCommand;
import com.amenicsystem.application.auth.dto.TokenResult;
import com.amenicsystem.application.port.out.JwtPort;
import com.amenicsystem.domain.shared.DomainException;
import com.amenicsystem.domain.usuario.*;
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
