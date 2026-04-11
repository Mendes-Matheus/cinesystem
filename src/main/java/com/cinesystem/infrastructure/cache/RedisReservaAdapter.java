package com.cinesystem.infrastructure.cache;

import com.cinesystem.application.port.out.ReservaAssentoPort;
import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.sessao.SessaoId;
import com.cinesystem.domain.usuario.UsuarioId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisReservaAdapter implements ReservaAssentoPort {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisReservaAdapter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean reservar(SessaoId sessaoId, AssentoId assentoId, UsuarioId usuarioId) {
        String key = "reserva:%d:%d".formatted(sessaoId.id(), assentoId.id());
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, usuarioId.id(), Duration.ofMinutes(10));
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void liberar(SessaoId sessaoId, AssentoId assentoId) {
        String key = "reserva:%d:%d".formatted(sessaoId.id(), assentoId.id());
        redisTemplate.delete(key);
    }
}
