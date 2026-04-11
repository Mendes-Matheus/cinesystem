package com.cinesystem.interfaces.http.admin;

import com.cinesystem.application.sessao.dto.RelatorioSessaoResult;
import com.cinesystem.application.usuario.dto.UsuarioResult;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class AdminHttpMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public UsuarioResponseDTO toUsuarioResponse(UsuarioResult result) {
        if (result == null) return null;
        return new UsuarioResponseDTO(
                result.id(),
                result.nome(),
                result.email(),
                result.role(),
                result.ativo(),
                result.criadoEm()
        );
    }

    public Page<UsuarioResponseDTO> toUsuarioPage(Page<UsuarioResult> page) {
        if (page == null) return null;
        return page.map(this::toUsuarioResponse);
    }

    public RelatorioSessaoResponseDTO toRelatorioResponse(RelatorioSessaoResult result) {
        if (result == null) return null;
        return new RelatorioSessaoResponseDTO(
                result.sessaoId(),
                result.tituloFilme(),
                result.dataHora().format(FORMATTER),
                result.totalAssentos(),
                result.assentosOcupados(),
                result.assentosDisponiveis(),
                result.receitaTotal()
        );
    }
}
