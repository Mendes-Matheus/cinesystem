package com.cinesystem.interfaces.http.filme;

import com.cinesystem.application.filme.dto.AtualizarFilmeCommand;
import com.cinesystem.application.filme.dto.CriarFilmeCommand;
import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.domain.filme.ClassificacaoEtaria;
import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.filme.Genero;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FilmeHttpMapper {

    public CriarFilmeCommand toCommand(FilmeRequestDTO dto) {
        if (dto == null) return null;
        return new CriarFilmeCommand(
                dto.titulo(),
                Genero.valueOf(dto.genero()),
                new ClassificacaoEtaria(dto.classificacao()),
                dto.duracaoMinutos(),
                dto.posterUrl(),
                dto.dataLancamento()
        );
    }

    public AtualizarFilmeCommand toUpdateCommand(Long id, FilmeRequestDTO dto) {
        if (dto == null) return null;
        return new AtualizarFilmeCommand(
                new FilmeId(id),
                dto.titulo(),
                Genero.valueOf(dto.genero()),
                new ClassificacaoEtaria(dto.classificacao()),
                dto.duracaoMinutos(),
                dto.posterUrl()
        );
    }

    public FilmeResponseDTO toResponse(FilmeResult result) {
        if (result == null) return null;
        return new FilmeResponseDTO(
                result.id(),
                result.titulo(),
                result.genero(),
                result.classificacao(),
                result.duracaoMinutos(),
                result.posterUrl(),
                result.dataLancamento()
        );
    }

    public List<FilmeResponseDTO> toResponseList(List<FilmeResult> results) {
        if (results == null) return null;
        return results.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
