package com.amenicsystem.domain.shared;

public class GatewayException extends DomainException {
    public GatewayException(String mensagem) { super(mensagem); }
    public GatewayException(String mensagem, Throwable cause) {
        super(mensagem);
        initCause(cause);
    }
}
