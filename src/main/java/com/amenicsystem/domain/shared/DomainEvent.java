package com.amenicsystem.domain.shared;

import java.time.LocalDateTime;

public interface DomainEvent {

    LocalDateTime occurredOn();

    default String eventType() {
        return this.getClass().getSimpleName();
    }
}
