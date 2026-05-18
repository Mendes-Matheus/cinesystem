package com.amenicsystem.interfaces.http.ingresso;

import com.amenicsystem.application.ingresso.dto.ComprarIngressoCommand;
import com.amenicsystem.application.ingresso.dto.IngressoBasicoResult;
import com.amenicsystem.application.ingresso.dto.IngressoResult;
import com.amenicsystem.application.ingresso.dto.IniciarCheckoutCommand;
import com.amenicsystem.domain.assento.AssentoId;
import com.amenicsystem.domain.sessao.SessaoId;
import com.amenicsystem.domain.usuario.UsuarioId;
import com.amenicsystem.interfaces.http.ingresso.dto.CheckoutRequestDTO;
import com.amenicsystem.interfaces.http.ingresso.dto.IngressoBasicoResponseDTO;
import com.amenicsystem.interfaces.http.ingresso.dto.IngressoRequestDTO;
import com.amenicsystem.interfaces.http.ingresso.dto.IngressoResponseDTO;
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
                dto.metodoPagamento(),
                dto.cpfCliente(),
                dto.nomeCliente()
        );
    }

    public IngressoBasicoResponseDTO toBasicoResponse(IngressoBasicoResult result) {
        if (result == null) return null;
        return new IngressoBasicoResponseDTO(
                result.id(),
                result.codigo(),
                result.valorPago(),
                result.status(),
                result.compradoEm(),
                result.qrCodePix(),
                result.qrCodePixBase64()
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

    public IniciarCheckoutCommand toCheckoutCommand(IngressoRequestDTO dto, Long usuarioId, String guestId) {
        if (dto == null) return null;

        return new IniciarCheckoutCommand(
                new SessaoId(dto.sessaoId()),
                new AssentoId(dto.assentoId()),
                new UsuarioId(usuarioId),
                guestId,
                dto.tipo(),
                dto.cpfCliente(),
                dto.nomeCliente()
        );
    }

    public IniciarCheckoutCommand toCheckoutCommand(CheckoutRequestDTO dto, Long usuarioId, String guestId) {
        if (dto == null) return null;

        return new IniciarCheckoutCommand(
                new SessaoId(dto.sessaoId()),
                new AssentoId(dto.assentoId()),
                new UsuarioId(usuarioId),
                guestId,
                dto.tipo(),
                dto.cpfCliente(),
                dto.nomeCliente()
        );
    }

    public List<IngressoResponseDTO> toResponseList(List<IngressoResult> results) {
        if (results == null) return null;
        return results.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
