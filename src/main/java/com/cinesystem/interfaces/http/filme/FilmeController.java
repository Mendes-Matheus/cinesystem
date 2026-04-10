package com.cinesystem.interfaces.http.filme;

import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.application.filme.usecase.*;
import com.cinesystem.domain.filme.FilmeId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/filmes")
public class FilmeController {

    private final ListarFilmesUseCase listarFilmesUseCase;
    private final BuscarFilmePorIdUseCase buscarFilmePorIdUseCase;
    private final CriarFilmeUseCase criarFilmeUseCase;
    private final AtualizarFilmeUseCase atualizarFilmeUseCase;
    private final DeletarFilmeUseCase deletarFilmeUseCase;
    private final FilmeHttpMapper mapper;

    public FilmeController(ListarFilmesUseCase listarFilmesUseCase,
                           BuscarFilmePorIdUseCase buscarFilmePorIdUseCase,
                           CriarFilmeUseCase criarFilmeUseCase,
                           AtualizarFilmeUseCase atualizarFilmeUseCase,
                           DeletarFilmeUseCase deletarFilmeUseCase,
                           FilmeHttpMapper mapper) {
        this.listarFilmesUseCase = listarFilmesUseCase;
        this.buscarFilmePorIdUseCase = buscarFilmePorIdUseCase;
        this.criarFilmeUseCase = criarFilmeUseCase;
        this.atualizarFilmeUseCase = atualizarFilmeUseCase;
        this.deletarFilmeUseCase = deletarFilmeUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<FilmeResponseDTO>> listarFilmes(@RequestParam(required = false) String genero) {
        List<FilmeResult> results = listarFilmesUseCase.execute(genero);
        return ResponseEntity.ok(mapper.toResponseList(results));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmeResponseDTO> buscarFilme(@PathVariable Long id) {
        FilmeResult result = buscarFilmePorIdUseCase.execute(new FilmeId(id));
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FilmeResponseDTO> criarFilme(@Valid @RequestBody FilmeRequestDTO request) {
        FilmeResult result = criarFilmeUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FilmeResponseDTO> atualizarFilme(@PathVariable Long id, @Valid @RequestBody FilmeRequestDTO request) {
        FilmeResult result = atualizarFilmeUseCase.execute(mapper.toUpdateCommand(id, request));
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletarFilme(@PathVariable Long id) {
        deletarFilmeUseCase.execute(new FilmeId(id));
        return ResponseEntity.noContent().build();
    }
}
