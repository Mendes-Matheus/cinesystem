package com.amenicsystem.interfaces.http.ingresso;


import com.amenicsystem.application.ingresso.dto.IniciarCheckoutCommand;
import com.amenicsystem.application.ingresso.usecase.IniciarCheckoutUseCase;
import com.amenicsystem.domain.assento.AssentoId;
import com.amenicsystem.domain.sessao.SessaoId;
import com.amenicsystem.domain.usuario.UsuarioId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final IniciarCheckoutUseCase checkoutUseCase;

    @PostMapping("/iniciar")
    @PreAuthorize("isAuthenticated()") // Checkout exige login
    public ResponseEntity<IngressoBasicoResponseDTO> iniciar(
            @RequestBody CheckoutRequestDTO request,
            @RequestHeader("X-Guest-ID") String guestId) {

        // Obtém o ID do usuário logado via SecurityContext
        Long authUserId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        var command = new IniciarCheckoutCommand(
                new SessaoId(request.sessaoId()),
                new AssentoId(request.assentoId()),
                new UsuarioId(authUserId),
                guestId,
                request.tipo()
        );

        var result = checkoutUseCase.execute(command);

        return ResponseEntity.ok(new IngressoBasicoResponseDTO(
                result.id(),
                result.codigo(),
                result.valorPago(),
                result.status(),
                result.compradoEm()
        ));
    }
}
