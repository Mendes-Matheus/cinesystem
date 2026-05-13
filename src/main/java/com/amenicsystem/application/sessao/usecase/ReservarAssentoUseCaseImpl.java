package com.amenicsystem.application.sessao.usecase;

import com.amenicsystem.application.port.out.ReservaAssentoPort;
import com.amenicsystem.application.sessao.dto.ReservarAssentoCommand;
import com.amenicsystem.domain.sessao.SessaoAssento;
import com.amenicsystem.domain.sessao.SessaoRepository;
import com.amenicsystem.domain.shared.DomainException;
import com.amenicsystem.domain.shared.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservarAssentoUseCaseImpl implements ReservarAssentoUseCase {

    private final SessaoRepository sessaoRepository;
    private final ReservaAssentoPort reservaPort;

    @Override
    @Transactional
    public void execute(ReservarAssentoCommand command) {
        // 1. Tenta o Lock distribuído no Redis (Evita concorrência na mesma fração de segundo)
        if (!reservaPort.reservar(command.sessaoId(), command.assentoId(), command.identificador())) {
            throw new DomainException("Este assento já está em processo de reserva.");
        }

        try {
            // 2. Busca o assento no BD
            SessaoAssento sessaoAssento = sessaoRepository.findSessaoAssento(command.sessaoId(), command.assentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sessão/Assento não encontrados"));

            // 3. Atualiza o status no Domínio (Reserva de 10 minutos)
            sessaoAssento.reservarTemporariamente(command.identificador(), 10);

            // 4. Salva a alteração no Banco de Dados
            sessaoRepository.saveAllAssentos(List.of(sessaoAssento));

        } catch (Exception e) {
            // Rollback manual do cache caso a persistência no banco falhe
            reservaPort.liberar(command.sessaoId(), command.assentoId());
            throw e;
        }
    }
}