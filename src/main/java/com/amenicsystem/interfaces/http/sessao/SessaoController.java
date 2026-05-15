package com.amenicsystem.interfaces.http.sessao;

import com.amenicsystem.application.sessao.dto.AssentoResult;
import com.amenicsystem.application.sessao.dto.CriarSessaoCommand;
import com.amenicsystem.application.sessao.dto.SessaoResult;
import com.amenicsystem.application.sessao.usecase.BuscarAssentosUseCase;
import com.amenicsystem.application.sessao.usecase.CancelarSessaoUseCase;
import com.amenicsystem.application.sessao.usecase.CriarSessaoUseCase;
import com.amenicsystem.application.sessao.usecase.ListarSessoesPorFilmeUseCase;
import com.amenicsystem.domain.filme.FilmeId;
import com.amenicsystem.domain.sessao.SessaoId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SessaoController {

    private final ListarSessoesPorFilmeUseCase listarSessoesPorFilmeUseCase;
    private final BuscarAssentosUseCase buscarAssentosUseCase;
    private final CriarSessaoUseCase criarSessaoUseCase;
    private final CancelarSessaoUseCase cancelarSessaoUseCase;
    private final SessaoHttpMapper mapper;

    @GetMapping("/api/v1/filmes/{filmeId}/sessoes")
    public ResponseEntity<List<SessaoResponseDTO>> listarPorFilme(@PathVariable Long filmeId) {
        List<SessaoResult> results = listarSessoesPorFilmeUseCase.execute(new FilmeId(filmeId));
        return ResponseEntity.ok(mapper.toResponseList(results));
    }

    @GetMapping("/api/v1/sessoes/{id}/assentos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AssentoResponseDTO>> listarAssentos(@PathVariable Long id) {
        List<AssentoResult> results = buscarAssentosUseCase.execute(new SessaoId(id));
        return ResponseEntity.ok(mapper.toAssentoResponseList(results));
    }

    @PostMapping("/api/v1/sessoes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessaoResponseDTO> criar(@Valid @RequestBody SessaoRequestDTO dto) {
        CriarSessaoCommand command = mapper.toCommand(dto);
        SessaoResult result = criarSessaoUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
    }

    @DeleteMapping("/api/v1/sessoes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        cancelarSessaoUseCase.execute(new SessaoId(id));
        return ResponseEntity.noContent().build();
    }
}
