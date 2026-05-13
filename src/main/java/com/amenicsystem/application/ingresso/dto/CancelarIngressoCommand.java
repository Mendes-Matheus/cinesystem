package com.amenicsystem.application.ingresso.dto;

import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.usuario.UsuarioId;

public record CancelarIngressoCommand(IngressoId ingressoId, UsuarioId usuarioId) {}
