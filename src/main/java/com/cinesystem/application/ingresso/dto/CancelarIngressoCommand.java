package com.cinesystem.application.ingresso.dto;

import com.cinesystem.domain.ingresso.IngressoId;
import com.cinesystem.domain.usuario.UsuarioId;

public record CancelarIngressoCommand(IngressoId ingressoId, UsuarioId usuarioId) {}
