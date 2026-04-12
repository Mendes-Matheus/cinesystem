package com.cinesystem;

import com.cinesystem.application.auth.dto.LoginCommand;
import com.cinesystem.application.auth.dto.TokenResult;
import com.cinesystem.domain.usuario.Role;
import com.cinesystem.infrastructure.persistence.usuario.UsuarioJpaEntity;
import com.cinesystem.infrastructure.persistence.usuario.UsuarioJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public abstract class CineSystemIntegrationTest {

    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cinesystem_test");

    static GenericContainer<?> redis =
        new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired protected TestRestTemplate restTemplate;
    @Autowired private UsuarioJpaRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    protected String loginComoAdmin() {
        return login("admin@cinesystem.com", "admin123", Role.ADMIN);
    }

    protected String loginComoCliente() {
        return login("cliente@cinesystem.com", "cliente123", Role.CLIENTE);
    }

    protected String loginComoClienteDois() {
        return login("cliente2@cinesystem.com", "cliente123", Role.CLIENTE);
    }

    private String login(String email, String senha, Role role) {
        // Criar usuário se não existir
        if (usuarioRepository.findByEmail(email).isEmpty()) {
            UsuarioJpaEntity entity = new UsuarioJpaEntity();
            entity.setNome("Test User");
            entity.setEmail(email);
            entity.setSenhaHash(passwordEncoder.encode(senha));
            entity.setRole(role);
            entity.setAtivo(true);
            usuarioRepository.save(entity);
        }

        ResponseEntity<TokenResult> response = restTemplate.postForEntity(
            "/api/v1/auth/login",
            new LoginCommand(email, senha),
            TokenResult.class
        );

        return response.getBody().accessToken();
    }

    protected HttpHeaders headersWithToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
