package com.amenicsystem.infrastructure.persistence.webhook;

import com.amenicsystem.application.port.out.ProcessedWebhookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProcessedWebhookRepositoryAdapter implements ProcessedWebhookRepository {

    private final ProcessedWebhookJpaRepository repository;

    @Override
    public boolean tentarRegistrar(String paymentId, String statusProcessado, String notificationId) {
        int rowsInserted = repository.insertIfAbsent(paymentId, statusProcessado, notificationId);
        return rowsInserted > 0;
    }
}
