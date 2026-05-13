package com.amenicsystem.domain.shared;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }
}
