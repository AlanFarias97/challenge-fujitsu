package com.alan.challenge.event_manager.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_events_event_id",
                columnNames = "event_id"
        )
)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "event_timestamp", nullable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private EventType eventType;

    @Column(nullable = false, length = 2000)
    private String description;

    protected Event() {
    }

    public Event(
            String eventId,
            Instant timestamp,
            EventType eventType,
            String description
    ) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }
}