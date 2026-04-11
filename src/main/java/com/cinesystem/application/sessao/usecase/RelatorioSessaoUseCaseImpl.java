package com.cinesystem.application.sessao.usecase;

import com.cinesystem.application.ingresso.dto.IngressoResult;
import com.cinesystem.application.port.out.query.IngressoQueryPort;
import com.cinesystem.application.port.out.query.SessaoQueryPort;
import com.cinesystem.application.sessao.dto.AssentoResult;
import com.cinesystem.application.sessao.dto.RelatorioSessaoResult;
import com.cinesystem.application.sessao.dto.SessaoResult;
import com.cinesystem.domain.sessao.SessaoId;
import com.cinesystem.domain.shared.ResourceNotFoundException;
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
