package com.cinesystem.interfaces.http.auth;

import com.cinesystem.application.auth.dto.TokenResult;
import com.cinesystem.application.auth.usecase.AutenticarUsuarioUseCase;
import com.cinesystem.application.auth.usecase.CadastrarUsuarioUseCase;
import com.cinesystem.application.port.out.JwtPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;
    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final JwtPort jwtPort;
    private final AuthHttpMapper mapper;

    @PostMapping("/cadastro")
    public ResponseEntity<AuthResponseDTO> cadastrar(@Valid @RequestBody CadastroRequestDTO dto) {
        TokenResult result = cadastrarUsuarioUseCase.execute(mapper.toCommand(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO dto) {
        TokenResult result = autenticarUsuarioUseCase.execute(mapper.toCommand(dto));
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtPort.revogar(token);
        }
        return ResponseEntity.noContent().build();
    }
}
