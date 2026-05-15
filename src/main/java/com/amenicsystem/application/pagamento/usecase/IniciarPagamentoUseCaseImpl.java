package com.amenicsystem.application.pagamento.usecase;

import com.amenicsystem.application.pagamento.dto.*;
import com.amenicsystem.application.port.out.CriarPagamentoRequest;
import com.amenicsystem.application.port.out.GatewayPagamentoResult;
import com.amenicsystem.application.port.out.PagamentoGatewayPort;
import com.amenicsystem.application.port.out.ReservaAssentoPort;
import com.amenicsystem.domain.assento.AssentoId;
import com.amenicsystem.domain.filme.FilmeRepository;
import com.amenicsystem.domain.ingresso.Ingresso;
import com.amenicsystem.domain.ingresso.IngressoRepository;
import com.amenicsystem.domain.pagamento.MetodoPagamento;
import com.amenicsystem.domain.pagamento.Pagamento;
import com.amenicsystem.domain.pagamento.PagamentoRepository;
import com.amenicsystem.domain.pagamento.StatusPagamento;
import com.amenicsystem.domain.sessao.SessaoId;
import com.amenicsystem.domain.sessao.SessaoRepository;
import com.amenicsystem.domain.shared.DomainException;
import com.amenicsystem.domain.shared.GatewayException;
import com.amenicsystem.domain.usuario.UsuarioId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import com.amenicsystem.application.pagamento.dto.IniciarPagamentoCommand;
import com.amenicsystem.application.pagamento.dto.IniciarPagamentoResult;
import com.amenicsystem.domain.ingresso.StatusIngresso;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IniciarPagamentoUseCaseImpl implements IniciarPagamentoUseCase {

    private final SessaoRepository sessaoRepository;
    private final FilmeRepository filmeRepository;
    private final IngressoRepository ingressoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final ReservaAssentoPort reservaPort;
    private final PagamentoGatewayPort pagamentoGatewayPort;

    @Override
    @Transactional
    public IniciarPagamentoResult execute(IniciarPagamentoCommand command) {
        log.info("Iniciando pagamento para usuário {} | sessão {} | assento {}",
                command.usuarioId(), command.sessaoId(), command.assentoId());

        var sessaoId = new SessaoId(command.sessaoId());
        var assentoId = new AssentoId(command.assentoId());
        var usuarioId = new UsuarioId(command.usuarioId());

        // 1. Carregar sessão, filme e assento
        var sessao = sessaoRepository.findById(sessaoId)
                .orElseThrow(() -> new DomainException("Sessão não encontrada"));
                
        var filme = filmeRepository.findById(sessao.getFilmeId())
                .orElseThrow(() -> new DomainException("Filme não encontrado"));

        var sessaoAssento = sessaoRepository
                .findSessaoAssento(sessaoId, assentoId)
                .orElseThrow(() -> new DomainException("Sessão ou assento não encontrado"));

        // O identificador da reserva pode ser o guestId ou o usuarioId
        String identificadorReserva = command.guestId() != null ? command.guestId() : String.valueOf(command.usuarioId());

        // 2. Reservar assento no Redis (atomic)
        boolean reservado = reservaPort.reservar(
                sessaoId, assentoId, identificadorReserva);
        if (!reservado) {
            throw new DomainException("Assento indisponível ou já reservado");
        }

        // 3. Confirmar compra no agregado e persistir
        var ingresso = sessaoAssento.confirmarCompra(usuarioId, command.guestId(), sessao);
        sessaoRepository.saveAllAssentos(List.of(sessaoAssento));

        // 4. Salvar o ingresso
        ingresso = ingressoRepository.save(ingresso);

        // 5. Montar título do item para exibição na tela do MP
        String tituloItem = String.format("Ingresso – %s – %s",
                filme.getTitulo(),
                sessao.getDataHora().toString());

        // 6. Criar Preference no Mercado Pago
        var gatewayRequest = new CriarPagamentoRequest(
                ingresso.getId().id(),
                tituloItem,
                ingresso.getValorPago(),
                command.emailPagador(),
                null,   // backUrlSucesso — o adapter usa o valor do application.yml
                null,   // backUrlFalha
                null,   // backUrlPendente
                null    // notificationUrl — o adapter usa o valor do application.yml
        );

        GatewayPagamentoResult gatewayResult;
        try {
            gatewayResult = pagamentoGatewayPort.criarPreference(gatewayRequest);
        } catch (GatewayException e) {
            log.error("Falha ao criar preference no MP para ingresso {}: {}",
                    ingresso.getId().id(), e.getMessage());
            // Salvar pagamento como REJEITADO para rastreabilidade
            var pagamentoRejeitado = Pagamento.builder()
                    .ingressoId(ingresso.getId())
                    .valor(ingresso.getValorPago())
                    .metodo(MetodoPagamento.CHECKOUT_PRO)
                    .status(StatusPagamento.REJEITADO)
                    .criadoEm(LocalDateTime.now())
                    .build();
            pagamentoRepository.save(pagamentoRejeitado);
            throw e;
        }

        // 7. Criar Pagamento PENDENTE com preferenceId como transacaoExternaId
        var pagamento = Pagamento.builder()
                .ingressoId(ingresso.getId())
                .transacaoExternaId(gatewayResult.preferenceId())
                .valor(ingresso.getValorPago())
                .metodo(MetodoPagamento.CHECKOUT_PRO)
                .status(StatusPagamento.PENDENTE)
                .criadoEm(LocalDateTime.now())
                .build();
        pagamento = pagamentoRepository.save(pagamento);

        log.info("Preference criada. preferenceId={}, ingressoId={}",
                gatewayResult.preferenceId(), ingresso.getId().id());

        // 8. Retornar resultado com redirectUrl para o frontend
        return new IniciarPagamentoResult(
                ingresso.getId().id(),
                ingresso.getCodigo().valor(),
                ingresso.getStatus().name(),
                gatewayResult.preferenceId(),
                pagamento.getStatus().name(),
                gatewayResult.redirectUrl()
        );
    }
}

