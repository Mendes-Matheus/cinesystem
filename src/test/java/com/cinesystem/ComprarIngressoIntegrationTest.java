package com.cinesystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ComprarIngressoIntegrationTest extends CineSystemIntegrationTest {

    private static final String GUEST_ID = "guest-test-abc123";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.cinesystem.application.port.out.PagamentoGatewayPort pagamentoGatewayPort;

    private Long sessaoId;
    private Long assentoId;

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
        // timestamp future
        jdbcTemplate.execute("INSERT INTO sessao (id, filme_id, sala_id, data_hora, idioma, formato, preco, status) VALUES (1, 1, 1, NOW() + INTERVAL '1 day', 'LEGENDADO', '_2D', 25.00, 'ATIVA')");
//        jdbcTemplate.execute("INSERT INTO sessao_assento (id, sessao_id, assento_id, status) VALUES (1, 1, 1, 'DISPONIVEL')");
        jdbcTemplate.execute("""
            INSERT INTO sessao_assento (id, sessao_id, assento_id, status, reserva_identificador)
            VALUES (1, 1, 1, 'RESERVADO', 'guest-test-abc123')
        """);

        this.sessaoId = 1L;
        this.assentoId = 1L;
        
        // clean up redis keys matching reserva
//        redisTemplate.delete("reserva:sessao:1:assento:1");
        redisTemplate.opsForValue().set("reserva:sessao:1:assento:1", GUEST_ID, Duration.ofMinutes(10));

        org.mockito.Mockito.when(pagamentoGatewayPort.processarPagamentoPix(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new com.cinesystem.application.port.out.TransacaoGatewayResult("tx-1", "qr", "qr-base64"));
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

    // Helper para headers com token + guestId
    private HttpHeaders headersWithTokenAndGuestId(String token) {
        HttpHeaders headers = headersWithToken(token);
        headers.set("X-Guest-ID", GUEST_ID);
        return headers;
    }


    @Test
    @DisplayName("Deve comprar ingresso e registrar no outbox")
    void deveComprarIngresso_ERegistrarNoOutbox() {
        String token = loginComoCliente();

        String payload = """
            {
                "sessaoId": 1,
                "assentoId": 1,
                "tipo": "INTEIRA",
                "metodoPagamento": "PIX"
            }
        """;

        HttpEntity<String> request = new HttpEntity<>(payload, headersWithTokenAndGuestId(token));

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/v1/ingressos", request, Map.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("status")).isEqualTo("AGUARDANDO_PAGAMENTO");

        // Verifica banco ingressos
        Integer cntIngresso = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ingresso WHERE status = 'AGUARDANDO_PAGAMENTO'", Integer.class);
        assertThat(cntIngresso).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve retornar 422 quando assento já reservado")
    void deveRetornar422_QuandoAssentoJaReservado() {
        // Reservar antes via Redis
        redisTemplate.opsForValue().set("reserva:sessao:1:assento:1", "10", Duration.ofMinutes(10));
        
        String token = loginComoCliente();

        String payload = """
            {
                "sessaoId": 1,
                "assentoId": 1,
                "tipo": "INTEIRA",
                "metodoPagamento": "PIX"
            }
        """;

        HttpEntity<String> request = new HttpEntity<>(payload, headersWithToken(token));


        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/ingressos", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(response.getBody()).contains("DOMAIN_ERROR");
    }

    @Test
    @DisplayName("Deve cancelar ingresso quando usuário é dono")
    void deveCancelarIngresso_QuandoUsuarioEhDono() {
        String token = loginComoCliente();

        String payload = """
            {
                "sessaoId": 1,
                "assentoId": 1,
                "tipo": "INTEIRA",
                "metodoPagamento": "PIX"
            }
        """;

        HttpEntity<String> request = new HttpEntity<>(payload, headersWithTokenAndGuestId(token));

        ResponseEntity<Map> postResponse = restTemplate.postForEntity("/api/v1/ingressos", request, Map.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Long ingressoId = ((Number) postResponse.getBody().get("id")).longValue();

        jdbcTemplate.execute("UPDATE pagamento SET status = 'APROVADO'");
        jdbcTemplate.execute("UPDATE ingresso SET status = 'ATIVO'");

        // DELETE
        ResponseEntity<Void> deleteResponse = restTemplate.exchange("/api/v1/ingressos/" + ingressoId, HttpMethod.DELETE, new HttpEntity<>(headersWithToken(token)), Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Map> getResponse = restTemplate.exchange(
                "/api/v1/ingressos/" + ingressoId, HttpMethod.GET,
                new HttpEntity<>(headersWithToken(token)), Map.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().get("status")).isEqualTo("CANCELADO"); // já estava assim ✅
    }

    @Test
    @DisplayName("Deve retornar 403 quando outro usuário tenta cancelar")
    void deveRetornar403_QuandoOutroUsuarioTentaCancelar() {
        String tokenDono = loginComoCliente();
        String tokenOutro = loginComoClienteDois();

        String payload = """
            {
                "sessaoId": 1,
                "assentoId": 1,
                "tipo": "INTEIRA",
                "metodoPagamento": "PIX"
            }
        """;

        HttpEntity<String> request = new HttpEntity<>(payload, headersWithTokenAndGuestId(tokenDono));
        request.getHeaders().add("Content-Type", "application/json");

        ResponseEntity<Map> postResponse = restTemplate.postForEntity("/api/v1/ingressos", request, Map.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED); // garante que o POST passou
        Long ingressoId = ((Number) postResponse.getBody().get("id")).longValue();

        jdbcTemplate.execute("UPDATE pagamento SET status = 'APROVADO'");
        jdbcTemplate.execute("UPDATE ingresso SET status = 'ATIVO'");

        // Outro user tentando deletar
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "/api/v1/ingressos/" + ingressoId, HttpMethod.DELETE,
                new HttpEntity<>(headersWithToken(tokenOutro)), String.class
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }
}
