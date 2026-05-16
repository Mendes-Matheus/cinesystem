package com.amenicsystem.domain.pagamento;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enum que representa os status brutos retornados pela API do Mercado Pago
 * e fornece mapeamento direto para o {@link StatusPagamento} do domínio.
 *
 * <p>Centraliza a conversão de status, eliminando strings mágicas espalhadas
 * pelo código e facilitando a extensão para novos status futuros.</p>
 *
 * <h3>Mapeamento MP → Domínio:</h3>
 * <pre>
 *   approved     → APROVADO
 *   pending      → PENDENTE
 *   authorized   → AUTORIZADO
 *   in_process   → EM_PROCESSO
 *   in_mediation → EM_MEDIACAO
 *   rejected     → REJEITADO
 *   cancelled    → CANCELADO
 *   refunded     → REEMBOLSADO
 *   charged_back → CONTESTADO
 * </pre>
 */
public enum StatusMercadoPago {

    APPROVED("approved", StatusPagamento.APROVADO),
    PENDING("pending", StatusPagamento.PENDENTE),
    AUTHORIZED("authorized", StatusPagamento.AUTORIZADO),
    IN_PROCESS("in_process", StatusPagamento.EM_PROCESSO),
    IN_MEDIATION("in_mediation", StatusPagamento.EM_MEDIACAO),
    REJECTED("rejected", StatusPagamento.REJEITADO),
    CANCELLED("cancelled", StatusPagamento.CANCELADO),
    REFUNDED("refunded", StatusPagamento.REEMBOLSADO),
    CHARGED_BACK("charged_back", StatusPagamento.CONTESTADO);

    private final String valorMercadoPago;
    private final StatusPagamento statusDominio;

    /** Lookup case-insensitive por valor bruto do MP. */
    private static final Map<String, StatusMercadoPago> LOOKUP =
            Stream.of(values())
                    .collect(Collectors.toUnmodifiableMap(
                            s -> s.valorMercadoPago.toLowerCase(),
                            s -> s));

    StatusMercadoPago(String valorMercadoPago, StatusPagamento statusDominio) {
        this.valorMercadoPago = valorMercadoPago;
        this.statusDominio = statusDominio;
    }

    /**
     * Converte o status bruto (string) retornado pela API do MP para este enum.
     *
     * @param raw status bruto (ex: "approved", "in_process")
     * @return o enum correspondente
     * @throws IllegalArgumentException se o status não for reconhecido
     */
    public static StatusMercadoPago fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Status do Mercado Pago não pode ser nulo ou vazio");
        }
        StatusMercadoPago result = LOOKUP.get(raw.toLowerCase().trim());
        if (result == null) {
            throw new IllegalArgumentException("Status desconhecido do Mercado Pago: " + raw);
        }
        return result;
    }

    /**
     * Tenta converter o status bruto, retornando {@code null} se não for reconhecido.
     * Útil para cenários onde status desconhecidos devem ser tratados sem exceção.
     *
     * @param raw status bruto
     * @return o enum correspondente ou {@code null}
     */
    public static StatusMercadoPago fromStringOrNull(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return LOOKUP.get(raw.toLowerCase().trim());
    }

    /** Retorna o {@link StatusPagamento} do domínio correspondente. */
    public StatusPagamento toDominio() {
        return statusDominio;
    }

    /** Retorna o valor bruto como recebido do Mercado Pago. */
    public String getValorMercadoPago() {
        return valorMercadoPago;
    }
}
