package com.cinesystem.application.sessao.dto;

import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.sessao.SessaoId;

public record ReservarAssentoCommand(
        SessaoId sessaoId,
        AssentoId assentoId,
        String identificador
) {}