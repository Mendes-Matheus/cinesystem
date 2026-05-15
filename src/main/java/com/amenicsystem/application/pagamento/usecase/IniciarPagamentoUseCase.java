package com.amenicsystem.application.pagamento.usecase;

import com.amenicsystem.application.pagamento.dto.IniciarPagamentoCommand;
import com.amenicsystem.application.pagamento.dto.IniciarPagamentoResult;

public interface IniciarPagamentoUseCase {
    IniciarPagamentoResult execute(IniciarPagamentoCommand command);
}
