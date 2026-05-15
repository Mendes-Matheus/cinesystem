package com.amenicsystem.domain.pagamento;

public enum StatusPagamento {
    PENDENTE,
    APROVADO,
    AUTORIZADO,
    EM_PROCESSO,
    EM_MEDIACAO,
    REJEITADO,
    CANCELADO,
    REEMBOLSADO,
    CONTESTADO,
    DESCONHECIDO // Status não mapeado — nunca alterar o Ingresso com este valor.
}

