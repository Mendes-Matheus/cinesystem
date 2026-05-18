package com.amenicsystem.interfaces.http.admin;

import com.amenicsystem.application.sessao.dto.RelatorioSessaoResult;
import com.amenicsystem.application.usuario.dto.UsuarioResult;
import com.amenicsystem.interfaces.http.admin.dto.RelatorioSessaoResponseDTO;
import com.amenicsystem.interfaces.http.admin.dto.UsuarioResponseDTO;
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
