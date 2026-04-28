package com.cinesystem.interfaces.http.ingresso;


import com.cinesystem.application.ingresso.dto.IniciarCheckoutCommand;
import com.cinesystem.application.ingresso.usecase.IniciarCheckoutUseCase;
import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.sessao.SessaoId;
import com.cinesystem.domain.usuario.UsuarioId;
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
                result.compradoEm(),
                result.qrCodePix(),
                result.qrCodePixBase64()
        ));
    }
}
