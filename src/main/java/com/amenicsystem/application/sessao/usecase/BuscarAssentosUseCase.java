package com.amenicsystem.application.sessao.usecase;

import com.amenicsystem.application.sessao.dto.AssentoResult;
import com.amenicsystem.domain.sessao.SessaoId;

import java.util.List;

public interface BuscarAssentosUseCase {
    List<AssentoResult> execute(SessaoId sessaoId);
}
