package com.distributed.ledger.domain.event;

import java.time.Instant;

/**
 * Base interface for all domain events.
 * Domain events represent something that happened in the domain.
 */
public interface DomainEvent {

    /**
     * @return Unique identifier for this event
     */
    String getEventId();

    /**
     * @return Timestamp when the event occurred
     */
    Instant getOccurredOn();

    /**
     * @return Type of the event
     */
    String getEventType();
}

