package com.amenicsystem.interfaces.http.pagamento;

import com.amenicsystem.application.pagamento.dto.IniciarPagamentoCommand;
import com.amenicsystem.application.pagamento.dto.PagamentoResult;
import com.amenicsystem.application.pagamento.usecase.BuscarPagamentoPorIngressoUseCase;
import com.amenicsystem.application.pagamento.usecase.IniciarPagamentoUseCase;
import com.amenicsystem.domain.ingresso.IngressoId;
import com.amenicsystem.domain.usuario.UsuarioId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pagamentos")
@RequiredArgsConstructor
@Slf4j
public class PagamentoController {

    private final IniciarPagamentoUseCase iniciarPagamentoUseCase;
    private final BuscarPagamentoPorIngressoUseCase buscarPagamentoUseCase;

    /**
     * Inicia o pagamento via Checkout Pro do Mercado Pago.
     *
     * Responde com HTTP 201 e a URL de redirecionamento (redirectUrl).
     * O frontend deve redirecionar o usuário para essa URL imediatamente.
     */
    @PostMapping("/iniciar")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<IniciarPagamentoResponseDTO> iniciarPagamento(
            @RequestHeader(value = "X-Guest-ID", required = false) String guestId,
            @Valid @RequestBody IniciarPagamentoRequestDTO dto) {

        Long usuarioId = getUsuarioAutenticado();
        log.info("Recebida requisição para iniciar pagamento. usuarioId={}, sessaoId={}, assentoId={}, guestId={}",
                usuarioId, dto.sessaoId(), dto.assentoId(), guestId);

        var command = new IniciarPagamentoCommand(
                dto.sessaoId(),
                dto.assentoId(),
                usuarioId,
                dto.emailPagador(),
                guestId
        );

        var result = iniciarPagamentoUseCase.execute(command);

        var response = new IniciarPagamentoResponseDTO(
                result.ingressoId(),
                result.codigoIngresso(),
                result.statusIngresso(),
                result.preferenceId(),
                result.statusPagamento(),
                result.redirectUrl()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Consulta o status do pagamento associado a um ingresso.
     * Apenas o dono do ingresso pode consultar.
     */
    @GetMapping("/ingresso/{ingressoId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PagamentoResult> buscarPorIngresso(
            @PathVariable Long ingressoId) {

        Long usuarioId = getUsuarioAutenticado();
        var result = buscarPagamentoUseCase.execute(
                new IngressoId(ingressoId), new UsuarioId(usuarioId));
        return ResponseEntity.ok(result);
    }

    private Long getUsuarioAutenticado() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(auth.getName());
    }
}

