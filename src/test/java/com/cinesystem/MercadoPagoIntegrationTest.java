package com.cinesystem;

import com.cinesystem.application.port.out.PagamentoGatewayPort;
import com.cinesystem.application.port.out.TransacaoGatewayResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SuppressWarnings("SqlResolve")
class MercadoPagoIntegrationTest extends CineSystemIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private PagamentoGatewayPort pagamentoGatewayPort;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM pagamento");
        jdbcTemplate.execute("DELETE FROM ingresso");
        jdbcTemplate.execute("DELETE FROM outbox_events");
        jdbcTemplate.execute("DELETE FROM sessao_assento");
        jdbcTemplate.execute("DELETE FROM sessao");
        jdbcTemplate.execute("DELETE FROM assento");
        jdbcTemplate.execute("DELETE FROM sala");
        jdbcTemplate.execute("DELETE FROM filme");
        jdbcTemplate.execute("DELETE FROM usuario");

        jdbcTemplate.execute("INSERT INTO filme (id, titulo, genero, classificacao, duracao_min, data_lancamento, ativo) VALUES (1, 'Duna', 'FICCAO', '12', 156, '2021-10-21', true)");
        jdbcTemplate.execute("INSERT INTO sala (id, nome, capacidade, tipo, ativa) VALUES (1, 'Sala 1', 100, '2D', true)");
        jdbcTemplate.execute("INSERT INTO assento (id, sala_id, fileira, numero, tipo) VALUES (1, 1, 'A', 1, 'STANDARD')");
        jdbcTemplate.execute("INSERT INTO sessao (id, filme_id, sala_id, data_hora, idioma, formato, preco, status) VALUES (1, 1, 1, NOW() + INTERVAL '1 day', 'LEGENDADO', '_2D', 25.00, 'ATIVA')");
        jdbcTemplate.execute("INSERT INTO sessao_assento (id, sessao_id, assento_id, status) VALUES (1, 1, 1, 'DISPONIVEL')");

        redisTemplate.delete("reserva:sessao:1:assento:1");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM pagamento");
        jdbcTemplate.execute("DELETE FROM ingresso");
        jdbcTemplate.execute("DELETE FROM outbox_events");
        jdbcTemplate.execute("DELETE FROM sessao_assento");
        jdbcTemplate.execute("DELETE FROM sessao");
        jdbcTemplate.execute("DELETE FROM assento");
        jdbcTemplate.execute("DELETE FROM sala");
        jdbcTemplate.execute("DELETE FROM filme");
        jdbcTemplate.execute("DELETE FROM usuario");
        redisTemplate.delete("reserva:sessao:1:assento:1");
    }

    @Test
    @DisplayName("Fluxo completo de checkout com Mercado Pago PIX")
    void fluxoCompletoCheckoutMercadoPagoPix() {
        // 1. Mock do Gateway do Mercado Pago
        String transacaoId = "mp-tx-999";
        when(pagamentoGatewayPort.processarPagamentoPix(any(), anyString()))
                .thenReturn(new TransacaoGatewayResult(transacaoId, "qr-code-pix-string", "base64-string"));

        // 2. Realizar reserva temporária (endpoint público)
        ResponseEntity<Void> reservaResponse = restTemplate.postForEntity(
                "/api/v1/sessoes/1/assentos/1/reservar",
                null,
                Void.class
        );

        assertThat(reservaResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        String guestId = reservaResponse.getHeaders().getFirst("X-Guest-ID");
        assertThat(guestId).isNotBlank();

        // 3. Realizar o checkout (endpoint autenticado)
        String token = loginComoCliente();
        String payloadCheckout = """
            {
                "sessaoId": 1,
                "assentoId": 1,
                "tipo": "INTEIRA",
                "metodoPagamento": "PIX"
            }
        """;

        var requestHeaders = headersWithToken(token);
        requestHeaders.add("X-Guest-ID", guestId);
        HttpEntity<String> checkoutRequest = new HttpEntity<>(payloadCheckout, requestHeaders);

        ResponseEntity<Map> checkoutResponse = restTemplate.postForEntity(
                "/api/v1/ingressos",
                checkoutRequest,
                Map.class
        );

        assertThat(checkoutResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<String, Object> body = checkoutResponse.getBody();
        assertThat(body.get("status")).isEqualTo("AGUARDANDO_PAGAMENTO");
        assertThat(body.get("qrCodePix")).isEqualTo("qr-code-pix-string");
        assertThat(body.get("qrCodePixBase64")).isEqualTo("base64-string");

        // Verifica banco de dados local após checkout
        Integer countPagamentosPendentes = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pagamento WHERE status = 'PENDENTE' AND transacao_id = ?", Integer.class, transacaoId);
        assertThat(countPagamentosPendentes).isEqualTo(1);

        String statusAssento = jdbcTemplate.queryForObject("SELECT status FROM sessao_assento WHERE id = 1", String.class);
        assertThat(statusAssento).isEqualTo("RESERVADO"); // Assento continua reservado aguardando o webhook

        // 4. Simular a chamada do Webhook do Mercado Pago
        String webhookPayload = """
            {
                "action": "payment.updated",
                "data": {
                    "id": "%s"
                }
            }
        """.formatted(transacaoId);

        HttpEntity<String> webhookRequest = new HttpEntity<>(webhookPayload, headersWithToken(token));
        ResponseEntity<Void> webhookResponse = restTemplate.postForEntity(
                "/api/v1/webhooks/mercadopago",
                webhookRequest,
                Void.class
        );

        assertThat(webhookResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verifica se assento mudou para OCUPADO
        String statusAssentoAposWebhook = jdbcTemplate.queryForObject("SELECT status FROM sessao_assento WHERE id = 1", String.class);
        assertThat(statusAssentoAposWebhook).isEqualTo("OCUPADO");

        // Verifica se ingresso mudou para ATIVO
        String statusIngresso = jdbcTemplate.queryForObject("SELECT status FROM ingresso", String.class);
        assertThat(statusIngresso).isEqualTo("ATIVO");
        
        // Verifica se pagamento mudou para APROVADO
        String statusPagamento = jdbcTemplate.queryForObject("SELECT status FROM pagamento", String.class);
        assertThat(statusPagamento).isEqualTo("APROVADO");
    }
}
