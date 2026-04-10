package com.cinesystem.application.ingresso.usecase;

import com.cinesystem.application.ingresso.dto.IngressoResult;
import com.cinesystem.application.port.out.query.IngressoQueryPort;
import com.cinesystem.domain.usuario.UsuarioId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarMeusIngressosUseCaseImpl implements ListarMeusIngressosUseCase {

    private final IngressoQueryPort ingressoQueryPort;

    public ListarMeusIngressosUseCaseImpl(IngressoQueryPort ingressoQueryPort) {
        this.ingressoQueryPort = ingressoQueryPort;
    }

    @Override
    public List<IngressoResult> execute(UsuarioId usuarioId) {
        return ingressoQueryPort.findByUsuario(usuarioId);
    }
}
