package com.amenicsystem.interfaces.http.admin;

import com.amenicsystem.application.sessao.dto.RelatorioSessaoResult;
import com.amenicsystem.application.sessao.usecase.RelatorioSessaoUseCase;
import com.amenicsystem.application.usuario.dto.UsuarioResult;
import com.amenicsystem.application.usuario.usecase.DesativarUsuarioUseCase;
import com.amenicsystem.application.usuario.usecase.ListarUsuariosUseCase;
import com.amenicsystem.domain.sessao.SessaoId;
import com.amenicsystem.domain.usuario.UsuarioId;
import com.amenicsystem.interfaces.http.admin.dto.RelatorioSessaoResponseDTO;
import com.amenicsystem.interfaces.http.admin.dto.UsuarioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ListarUsuariosUseCase listarUsuariosUseCase;
    private final DesativarUsuarioUseCase desativarUsuarioUseCase;
    private final RelatorioSessaoUseCase relatorioSessaoUseCase;
    private final AdminHttpMapper mapper;

    @GetMapping("/usuarios")
    public ResponseEntity<Page<UsuarioResponseDTO>> listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UsuarioResult> results = listarUsuariosUseCase.execute(pageable);
        return ResponseEntity.ok(mapper.toUsuarioPage(results));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> desativarUsuario(@PathVariable Long id, Authentication auth) {
        Long adminId = Long.parseLong(auth.getName());
        desativarUsuarioUseCase.execute(new UsuarioId(id), new UsuarioId(adminId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessoes/{id}/relatorio")
    public ResponseEntity<RelatorioSessaoResponseDTO> relatorioDaSessao(@PathVariable Long id) {
        RelatorioSessaoResult result = relatorioSessaoUseCase.execute(new SessaoId(id));
        return ResponseEntity.ok(mapper.toRelatorioResponse(result));
    }
}
