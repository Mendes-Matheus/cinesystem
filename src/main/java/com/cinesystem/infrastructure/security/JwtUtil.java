package com.cinesystem.infrastructure.security;

import com.cinesystem.application.auth.dto.TokenResult;
import com.cinesystem.application.port.out.JwtPort;
import com.cinesystem.domain.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil implements JwtPort {

    private final String secret;
    private final int expirationMinutes;
    private final RedisTemplate<String, Object> redisTemplate;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-minutes:15}") int expirationMinutes,
                   RedisTemplate<String, Object> redisTemplate) {
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
        this.redisTemplate = redisTemplate;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public TokenResult gerar(Usuario usuario) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (long) expirationMinutes * 60 * 1000);
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .subject(usuario.getEmail().valor())
                .claim("role", usuario.getRole().name())
                .claim("userId", usuario.getId().id())
                .id(jti)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

        return TokenResult.of(token, (long) expirationMinutes * 60);
    }

    @Override
    public String extrairEmail(String token) {
        return getClaims(token).getSubject();
    }

    @Override
    public boolean isValido(String token) {
        try {
            getClaims(token);
            return !isRevogado(token);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void revogar(String token) {
        Claims claims = getClaims(token);
        String jti = claims.getId();
        Date expiration = claims.getExpiration();
        long remainingMillis = expiration.getTime() - System.currentTimeMillis();

        if (remainingMillis > 0) {
            redisTemplate.opsForValue().set("token:blacklist:" + jti, "1", Duration.ofMillis(remainingMillis));
        }
    }

    @Override
    public boolean isRevogado(String token) {
        try {
            String jti = getClaims(token).getId();
            return Boolean.TRUE.equals(redisTemplate.hasKey("token:blacklist:" + jti));
        } catch (Exception e) {
            return true;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
