package com.amenicsystem.application.sessao.usecase;

import com.amenicsystem.application.ingresso.dto.IngressoResult;
import com.amenicsystem.application.port.out.query.IngressoQueryPort;
import com.amenicsystem.application.port.out.query.SessaoQueryPort;
import com.amenicsystem.application.sessao.dto.AssentoResult;
import com.amenicsystem.application.sessao.dto.RelatorioSessaoResult;
import com.amenicsystem.application.sessao.dto.SessaoResult;
import com.amenicsystem.domain.sessao.SessaoId;
import com.amenicsystem.domain.shared.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioSessaoUseCaseImpl implements RelatorioSessaoUseCase {

    private final SessaoQueryPort sessaoQueryPort;
    private final IngressoQueryPort ingressoQueryPort;

    @Override
    public RelatorioSessaoResult execute(SessaoId id) {
        SessaoResult sessao = sessaoQueryPort.findResultById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada: " + id.id()));

        List<AssentoResult> assentos = sessaoQueryPort.findAssentosBySessao(id);
        int totalAssentos = assentos.size();
        int assentosOcupados = (int) assentos.stream()
                .filter(a -> "RESERVADO".equals(a.status()) || "VENDIDO".equals(a.status()))
                .count();
        int assentosDisponiveis = totalAssentos - assentosOcupados;

        List<IngressoResult> ingressos = ingressoQueryPort.findBySessaoId(id);
        BigDecimal receitaTotal = ingressos.stream()
                .filter(i -> "ATIVO".equals(i.status()) || "UTILIZADO".equals(i.status()))
                .map(IngressoResult::valorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RelatorioSessaoResult(
                sessao.id(),
                sessao.tituloFilme(),
                sessao.dataHora(),
                totalAssentos,
                assentosOcupados,
                assentosDisponiveis,
                receitaTotal
        );
    }
}
