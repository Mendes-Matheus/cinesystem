package com.amenicsystem.infrastructure.payment.mercadopago;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MercadoPagoClientConfig {

    /**
     * Access token configurado em application.yml como:
     *
     *   api:
     *     v1:
     *       mercadopago-access-token: ${MERCADOPAGO_ACCESS_TOKEN}
     *
     * Para sandbox: usar token iniciado com "TEST-"
     */
    @Value("${mercado-pago.access-token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("Mercado Pago SDK inicializado com sucesso.");
    }
}
