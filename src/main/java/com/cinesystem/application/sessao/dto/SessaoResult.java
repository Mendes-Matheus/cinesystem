package com.cinesystem.application.sessao.dto;

import com.cinesystem.domain.sessao.Sessao;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessaoResult(
    Long id, Long filmeId, String tituloFilme, Long salaId, String nomeSala,
    LocalDateTime dataHora, String idioma, String formato,
    BigDecimal preco, String status, int assentosDisponiveis
) {
    public static SessaoResult from(Sessao sessao, String tituloFilme, String nomeSala, int assentosDisponiveis) {
        return new SessaoResult(
                sessao.getId() != null ? sessao.getId().id() : null,
                sessao.getFilmeId().id(),
                tituloFilme,
                sessao.getSalaId().id(),
                nomeSala,
                sessao.getDataHora(),
                sessao.getIdioma(),
                sessao.getFormato().name(),
                sessao.getPreco(),
                sessao.getStatus().name(),
                assentosDisponiveis
        );
    }
}
