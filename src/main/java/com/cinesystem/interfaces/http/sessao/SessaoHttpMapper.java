package com.cinesystem.interfaces.http.sessao;

import com.cinesystem.application.sessao.dto.AssentoResult;
import com.cinesystem.application.sessao.dto.CriarSessaoCommand;
import com.cinesystem.application.sessao.dto.SessaoResult;
import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.sala.SalaId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SessaoHttpMapper {

    public CriarSessaoCommand toCommand(SessaoRequestDTO dto) {
        if (dto == null) return null;
        return new CriarSessaoCommand(
                new FilmeId(dto.filmeId()),
                new SalaId(dto.salaId()),
                dto.dataHora(),
                dto.idioma(),
                dto.formato(),
                dto.preco()
        );
    }

    public SessaoResponseDTO toResponse(SessaoResult result) {
        if (result == null) return null;
        return new SessaoResponseDTO(
                result.id(),
                result.filmeId(),
                result.tituloFilme(),
                result.salaId(),
                result.nomeSala(),
                result.dataHora(),
                result.idioma(),
                result.formato(),
                result.preco(),
                result.status(),
                result.assentosDisponiveis()
        );
    }

    public List<SessaoResponseDTO> toResponseList(List<SessaoResult> results) {
        if (results == null) return null;
        return results.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AssentoResponseDTO toAssentoResponse(AssentoResult result) {
        if (result == null) return null;
        return new AssentoResponseDTO(
                result.id(),
                result.fileira(),
                result.numero(),
                result.tipo(),
                result.status()
        );
    }

    public List<AssentoResponseDTO> toAssentoResponseList(List<AssentoResult> results) {
        if (results == null) return null;
        return results.stream()
                .map(this::toAssentoResponse)
                .collect(Collectors.toList());
    }
}
