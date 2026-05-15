package com.amenicsystem.infrastructure.payment.mercadopago;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoConfigCustom {

    @Value("${mercado-pago.access-token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }
}
