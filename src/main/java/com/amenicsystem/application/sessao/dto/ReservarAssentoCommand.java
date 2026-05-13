package com.amenicsystem.application.sessao.dto;

import com.amenicsystem.domain.assento.AssentoId;
import com.amenicsystem.domain.sessao.SessaoId;

public record ReservarAssentoCommand(
        SessaoId sessaoId,
        AssentoId assentoId,
        String identificador
) {}