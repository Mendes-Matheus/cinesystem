package com.cinesystem.domain.ingresso;

import com.cinesystem.domain.usuario.UsuarioId;
import java.math.BigDecimal;

public class Ingresso {
    private CodigoIngresso codigo;
    private UsuarioId usuarioId;
    private Long sessaoAssentoId;
    private BigDecimal preco;
    
    public Ingresso(CodigoIngresso codigo, UsuarioId usuarioId, Long sessaoAssentoId, BigDecimal preco) {
        this.codigo = codigo;
        this.usuarioId = usuarioId;
        this.sessaoAssentoId = sessaoAssentoId;
        this.preco = preco;
    }
}
