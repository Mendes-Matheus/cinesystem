package com.cinesystem.application.sessao.usecase;

import com.cinesystem.application.sessao.dto.AssentoResult;
import com.cinesystem.domain.sessao.SessaoId;

import java.util.List;

public interface BuscarAssentosUseCase {
    List<AssentoResult> execute(SessaoId sessaoId);
}
