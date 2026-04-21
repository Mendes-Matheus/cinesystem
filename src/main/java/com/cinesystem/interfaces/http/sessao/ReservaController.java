package com.cinesystem.interfaces.http.sessao;

import com.cinesystem.application.sessao.dto.ReservarAssentoCommand;
import com.cinesystem.application.sessao.usecase.ReservarAssentoUseCase;
import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.sessao.SessaoId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessoes")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservarAssentoUseCase reservarAssentoUseCase;

    /**
     * Realiza a reserva temporária.
     * @param guestId Identificador único do visitante (enviado via Header).
     * Se não enviado, o sistema pode gerar um ou retornar erro.
     */
    @PostMapping("/{sessaoId}/assentos/{assentoId}/reservar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> reservar(
            @PathVariable Long sessaoId,
            @PathVariable Long assentoId,
            @RequestHeader(value = "X-Guest-ID", required = false) String guestId) {

        // Fallback: Se o front-end não enviar, geramos um temporário para a transação
        // No entanto, o ideal é que o front-end envie um UUID fixo por sessão de navegação.
        String identificadorEfetivo = (guestId != null && !guestId.isBlank())
                ? guestId
                : UUID.randomUUID().toString();

        var command = new ReservarAssentoCommand(
                new SessaoId(sessaoId),
                new AssentoId(assentoId),
                identificadorEfetivo
        );

        reservarAssentoUseCase.execute(command);

        // Retornamos o identificador no header para que o front-end saiba qual ID usar no checkout
        return ResponseEntity.noContent()
                .header("X-Guest-ID", identificadorEfetivo)
                .build();
    }
}