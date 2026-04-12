package com.cinesystem;

import com.cinesystem.application.filme.dto.CriarFilmeCommand;
import com.cinesystem.domain.filme.ClassificacaoEtaria;
import com.cinesystem.domain.filme.Genero;
import com.cinesystem.infrastructure.persistence.filme.FilmeJpaRepository;
import com.cinesystem.interfaces.http.filme.FilmeResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FilmeIntegrationTest extends CineSystemIntegrationTest {

    @Autowired
    private FilmeJpaRepository filmeJpaRepository;

    @AfterEach
    void tearDown() {
        filmeJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve listar filmes vazio quando nenhum cadastrado")
    void deveListarFilmesVazia_QuandoNenhumCadastrado() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/filmes", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    @Test
    @DisplayName("Deve criar filme com autenticação de Admin")
    void deveCriarFilme_ComAutenticacaoAdmin() {
        String token = loginComoAdmin();

        var command = new CriarFilmeCommand("Matrix", Genero.ACAO, new ClassificacaoEtaria("12"), 136, "http://poster", LocalDate.now());
        
        // CUIDADO: CriarFilmeCommand no pacote dto espera Strings se tiver sido alterado mas
        // No meu commit anterior arrumamos pra ser record CriarFilmeCommand(String titulo, Genero genero, ClassificacaoEtaria classificacao...)
        // Vamos checar o record CriarFilmeCommand.
        // Wait, the API endpoint is defined in `FilmeController`. It probably expects JSON, and Spring will map "ACAO" to Genero enum.
        // Let's create a Request DTO or just pass a String JSON / Map to avoid deserialization problems.
        
        String payload = """
            {
                "titulo": "Matrix",
                "genero": "ACAO",
                "classificacao": "12",
                "duracaoMinutos": 136,
                "posterUrl": "http://poster",
                "dataLancamento": "1999-03-31"
            }
        """;

        HttpEntity<String> request = new HttpEntity<>(payload, headersWithToken(token));


        ResponseEntity<String> postResponse = restTemplate.postForEntity("/api/v1/filmes", request, String.class);
        
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Extract ID from Location or assuming there's only 1 item
        ResponseEntity<FilmeResponseDTO[]> getResponse =
                restTemplate.getForEntity("/api/v1/filmes", FilmeResponseDTO[].class);

        assertThat(getResponse.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar 403 quando cliente tenta criar filme")
    void deveRetornar403_QuandoClienteTentaCriarFilme() {
        String token = loginComoCliente();

        String payload = """
            {
                "titulo": "Matrix",
                "genero": "ACAO",
                "classificacao": "12",
                "duracaoMinutos": 136,
                "posterUrl": "http://poster",
                "dataLancamento": "1999-03-31"
            }
        """;

        HttpEntity<String> request = new HttpEntity<>(payload, headersWithToken(token));


        ResponseEntity<String> postResponse = restTemplate.postForEntity("/api/v1/filmes", request, String.class);
        
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Deve retornar 422 quando dados inválidos")
    void deveRetornar422_QuandoDadosInvalidos() {
        String token = loginComoAdmin();

        String payload = """
            {
                "titulo": "Matrix",
                "genero": "ACAO",
                "classificacao": "12",
                "duracaoMinutos": -1,
                "posterUrl": "http://poster",
                "dataLancamento": "1999-03-31"
            }
        """;

        HttpEntity<String> request = new HttpEntity<>(payload, headersWithToken(token));
        request.getHeaders().add("Content-Type", "application/json");

        ResponseEntity<String> postResponse = restTemplate.postForEntity("/api/v1/filmes", request, String.class);
        
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(postResponse.getBody()).contains("DOMAIN_ERROR");
    }
}
