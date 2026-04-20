package com.cinesystem.interfaces.scheduler;

import com.cinesystem.domain.sessao.SessaoAssento;
import com.cinesystem.domain.sessao.SessaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservaExpiradaScheduler {

    private final SessaoRepository sessaoRepository;

    // Executa a cada 60 segundos
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void liberarAssentosExpirados() {
        LocalDateTime agora = LocalDateTime.now();
        List<SessaoAssento> expiradas = sessaoRepository.findReservasExpiradas(agora);

        if (!expiradas.isEmpty()) {
            log.info("Encontradas {} reservas de assentos expiradas. Iniciando liberação...", expiradas.size());

            expiradas.forEach(SessaoAssento::liberarReservaExpirada);

            // O saveAllAssentos processa a lista em lote (batch), otimizando a ida ao banco
            sessaoRepository.saveAllAssentos(expiradas);

            log.info("Assentos liberados com sucesso e retornados para DISPONIVEL.");
        }
    }
}