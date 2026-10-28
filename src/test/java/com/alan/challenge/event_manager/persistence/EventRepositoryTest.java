package com.alan.challenge.event_manager.persistence;

import com.alan.challenge.event_manager.domain.Event;
import com.alan.challenge.event_manager.domain.EventType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveProductionEvent() {
        // Given
        Instant timestamp = Instant.parse("2026-10-28T13:00:00Z");

        Event event = new Event(
                "EVT-1001",
                timestamp,
                EventType.PRODUCTION,
                "Inicio de la línea de producción"
        );

        // When
        Event savedEvent = eventRepository.saveAndFlush(event);
        Long savedId = savedEvent.getId();

        entityManager.clear();

        Event retrievedEvent = eventRepository.findById(savedId)
                .orElseThrow();

        // Then
        assertAll(
                () -> assertNotNull(retrievedEvent.getId()),
                () -> assertEquals("EVT-1001", retrievedEvent.getEventId()),
                () -> assertEquals(timestamp, retrievedEvent.getTimestamp()),
                () -> assertEquals(
                        EventType.PRODUCTION,
                        retrievedEvent.getEventType()
                ),
                () -> assertEquals(
                        "Inicio de la línea de producción",
                        retrievedEvent.getDescription()
                )
        );
    }
}