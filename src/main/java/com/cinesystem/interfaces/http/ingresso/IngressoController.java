package com.cinesystem.interfaces.http.ingresso;

import com.cinesystem.application.ingresso.dto.CancelarIngressoCommand;
import com.cinesystem.application.ingresso.dto.IngressoBasicoResult;
import com.cinesystem.application.ingresso.dto.IngressoResult;
import com.cinesystem.application.ingresso.dto.IniciarCheckoutCommand;
import com.cinesystem.application.ingresso.usecase.BuscarIngressoPorIdUseCase;
import com.cinesystem.application.ingresso.usecase.CancelarIngressoUseCase;
import com.cinesystem.application.ingresso.usecase.ComprarIngressoUseCase;
import com.cinesystem.application.ingresso.usecase.ListarMeusIngressosUseCase;
import com.cinesystem.domain.ingresso.IngressoId;
import com.cinesystem.domain.usuario.UsuarioId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingressos")
public class IngressoController {

    private final ComprarIngressoUseCase comprarIngressoUseCase;
    private final CancelarIngressoUseCase cancelarIngressoUseCase;
    private final ListarMeusIngressosUseCase listarMeusIngressosUseCase;
    private final BuscarIngressoPorIdUseCase buscarIngressoPorIdUseCase;
    private final IngressoHttpMapper mapper;

    public IngressoController(ComprarIngressoUseCase comprarIngressoUseCase,
                              CancelarIngressoUseCase cancelarIngressoUseCase,
                              ListarMeusIngressosUseCase listarMeusIngressosUseCase,
                              BuscarIngressoPorIdUseCase buscarIngressoPorIdUseCase,
                              IngressoHttpMapper mapper) {
        this.comprarIngressoUseCase = comprarIngressoUseCase;
        this.cancelarIngressoUseCase = cancelarIngressoUseCase;
        this.listarMeusIngressosUseCase = listarMeusIngressosUseCase;
        this.buscarIngressoPorIdUseCase = buscarIngressoPorIdUseCase;
        this.mapper = mapper;
    }

    private Long getUsuarioAutenticado() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getName());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IngressoBasicoResponseDTO> comprar(
            @Valid @RequestBody IngressoRequestDTO dto,
            @RequestHeader(value = "X-Guest-ID", required = false) String guestId) {

        Long usuarioId = getUsuarioAutenticado();

        // CORREÇÃO: O mapper agora deve montar o IniciarCheckoutCommand
        // Certifique-se de que o metodo 'toCommand' no seu IngressoHttpMapper
        // foi atualizado para receber o guestId e o TipoIngresso.
        var command = mapper.toCheckoutCommand(dto, usuarioId, guestId);

        IngressoBasicoResult result = comprarIngressoUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toBasicoResponse(result));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        Long usuarioId = getUsuarioAutenticado();
        cancelarIngressoUseCase.execute(new CancelarIngressoCommand(new IngressoId(id), new UsuarioId(usuarioId)));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/meus")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<IngressoResponseDTO>> listarMeusIngressos() {
        Long usuarioId = getUsuarioAutenticado();
        List<IngressoResult> results = listarMeusIngressosUseCase.execute(new UsuarioId(usuarioId));
        return ResponseEntity.ok(mapper.toResponseList(results));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IngressoResponseDTO> buscarPorId(@PathVariable Long id) {
        Long usuarioId = getUsuarioAutenticado();
        IngressoResult result = buscarIngressoPorIdUseCase.execute(new IngressoId(id), new UsuarioId(usuarioId));
        return ResponseEntity.ok(mapper.toResponse(result));
    }
}
