package com.amenicsystem.application.ingresso.usecase;

import com.amenicsystem.application.ingresso.dto.IngressoResult;
import com.amenicsystem.application.port.out.query.IngressoQueryPort;
import com.amenicsystem.domain.usuario.UsuarioId;
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
