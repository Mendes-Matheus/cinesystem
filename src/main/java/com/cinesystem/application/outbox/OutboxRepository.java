package com.cinesystem.application.outbox;

import java.util.List;

public interface OutboxRepository {
    OutboxEvent save(OutboxEvent event);
    List<OutboxEvent> findPendentes(int limit);
}
