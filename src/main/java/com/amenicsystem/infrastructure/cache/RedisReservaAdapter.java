package com.amenicsystem.infrastructure.cache;

import com.amenicsystem.application.port.out.ReservaAssentoPort;
import com.amenicsystem.domain.assento.AssentoId;
import com.amenicsystem.domain.sessao.SessaoId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisReservaAdapter implements ReservaAssentoPort {

    private final StringRedisTemplate redisTemplate;

    public RedisReservaAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean reservar(SessaoId sessaoId, AssentoId assentoId, String identificador) {
        String key = reservationKey(sessaoId, assentoId);
        // Salva a String genérica (pode ser o guestId ou usuarioId)
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, identificador, Duration.ofMinutes(10));
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void liberar(SessaoId sessaoId, AssentoId assentoId) {
        redisTemplate.delete(reservationKey(sessaoId, assentoId));
    }

    private String reservationKey(SessaoId sessaoId, AssentoId assentoId) {
        return "reserva:sessao:%d:assento:%d".formatted(sessaoId.id(), assentoId.id());
    }
}