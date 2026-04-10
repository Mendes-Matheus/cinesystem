package com.cinesystem.interfaces.http.ingresso;

import com.cinesystem.application.ingresso.dto.ComprarIngressoCommand;
import com.cinesystem.application.ingresso.dto.IngressoBasicoResult;
import com.cinesystem.application.ingresso.dto.IngressoResult;
import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.sessao.SessaoId;
import com.cinesystem.domain.usuario.UsuarioId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class IngressoHttpMapper {

    public ComprarIngressoCommand toCommand(IngressoRequestDTO dto, Long usuarioId) {
        if (dto == null) return null;
        return new ComprarIngressoCommand(
                new SessaoId(dto.sessaoId()),
                new AssentoId(dto.assentoId()),
                new UsuarioId(usuarioId),
                dto.metodoPagamento()
        );
    }

    public IngressoBasicoResponseDTO toBasicoResponse(IngressoBasicoResult result) {
        if (result == null) return null;
        return new IngressoBasicoResponseDTO(
                result.id(),
                result.codigo(),
                result.valorPago(),
                result.status(),
                result.compradoEm()
        );
    }

    public IngressoResponseDTO toResponse(IngressoResult result) {
        if (result == null) return null;
        return new IngressoResponseDTO(
                result.id(),
                result.codigo(),
                result.sessaoId(),
                result.assentoId(),
                result.fileira(),
                result.numeroAssento(),
                result.tituloFilme(),
                result.dataHora(),
                result.valorPago(),
                result.status()
        );
    }

    public List<IngressoResponseDTO> toResponseList(List<IngressoResult> results) {
        if (results == null) return null;
        return results.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
