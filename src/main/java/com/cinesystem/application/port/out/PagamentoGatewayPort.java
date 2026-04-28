package com.cinesystem.application.port.out;

import com.cinesystem.domain.pagamento.Pagamento;

public interface PagamentoGatewayPort {
    TransacaoGatewayResult processarPagamentoPix(Pagamento pagamento, String emailCliente);
}
