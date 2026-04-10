package com.cinesystem.domain.filme;

import com.cinesystem.domain.shared.DomainException;
import java.util.Set;

public record ClassificacaoEtaria(String codigo) {
    
    private static final Set<String> VALIDOS = Set.of("L", "10", "12", "14", "16", "18");

    public ClassificacaoEtaria {
        if (codigo == null || !VALIDOS.contains(codigo)) {
            throw new DomainException("Classificação inválida: " + codigo);
        }
    }

    public static ClassificacaoEtaria of(String codigo) {
        return new ClassificacaoEtaria(codigo);
    }
}
