package com.cinesystem.interfaces.http.sessao;

import com.cinesystem.application.sessao.dto.AssentoResult;
import com.cinesystem.application.sessao.dto.CriarSessaoCommand;
import com.cinesystem.application.sessao.dto.SessaoResult;
import com.cinesystem.application.sessao.usecase.BuscarAssentosUseCase;
import com.cinesystem.application.sessao.usecase.CancelarSessaoUseCase;
import com.cinesystem.application.sessao.usecase.CriarSessaoUseCase;
import com.cinesystem.application.sessao.usecase.ListarSessoesPorFilmeUseCase;
import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.sessao.SessaoId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SessaoController {

    private final ListarSessoesPorFilmeUseCase listarSessoesPorFilmeUseCase;
    private final BuscarAssentosUseCase buscarAssentosUseCase;
    private final CriarSessaoUseCase criarSessaoUseCase;
    private final CancelarSessaoUseCase cancelarSessaoUseCase;
    private final SessaoHttpMapper mapper;

    public SessaoController(ListarSessoesPorFilmeUseCase listarSessoesPorFilmeUseCase,
                            BuscarAssentosUseCase buscarAssentosUseCase,
                            CriarSessaoUseCase criarSessaoUseCase,
                            CancelarSessaoUseCase cancelarSessaoUseCase,
                            SessaoHttpMapper mapper) {
        this.listarSessoesPorFilmeUseCase = listarSessoesPorFilmeUseCase;
        this.buscarAssentosUseCase = buscarAssentosUseCase;
        this.criarSessaoUseCase = criarSessaoUseCase;
        this.cancelarSessaoUseCase = cancelarSessaoUseCase;
        this.mapper = mapper;
    }

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
