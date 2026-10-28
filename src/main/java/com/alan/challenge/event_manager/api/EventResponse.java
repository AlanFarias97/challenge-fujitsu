package com.alan.challenge.event_manager.api;

import com.alan.challenge.event_manager.domain.Event;
import com.alan.challenge.event_manager.domain.EventType;

import java.time.Instant;

public record EventResponse(
        Long id,
        String eventId,
        Instant timestamp,
        EventType eventType,
        String description
) {

    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getEventId(),
                event.getTimestamp(),
                event.getEventType(),
                event.getDescription()
        );
    }
}