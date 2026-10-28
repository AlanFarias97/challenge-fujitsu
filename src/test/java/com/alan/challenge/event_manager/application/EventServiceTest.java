package com.alan.challenge.event_manager.application;

import com.alan.challenge.event_manager.domain.Event;
import com.alan.challenge.event_manager.domain.EventAudit;
import com.alan.challenge.event_manager.domain.EventType;
import com.alan.challenge.event_manager.persistence.EventAuditRepository;
import com.alan.challenge.event_manager.persistence.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.alan.challenge.event_manager.domain.DuplicateEventException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventAuditRepository eventAuditRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void shouldRegisterProductionEventWithoutAudit() {
        // Given: un evento de producción válido
        Event event = new Event(
                "EVT-1001",
                Instant.parse("2026-10-28T13:00:00Z"),
                EventType.PRODUCTION,
                "Inicio de la línea de producción"
        );

        // When: solicitamos registrarlo
        eventService.register(event);

        // Then: se guarda el evento una sola vez
        verify(eventRepository).save(event);

        // Y no se utiliza el repositorio de auditoría
        verifyNoInteractions(eventAuditRepository);
    }

    @Test
    void shouldRegisterQualityEventWithAudit() {
        // Given
        Event event = new Event(
                "EVT-1002",
                Instant.parse("2026-10-28T14:00:00Z"),
                EventType.QUALITY,
                "Control de calidad aprobado"
        );

        // When
        when(eventRepository.save(event)).thenReturn(event);
        eventService.register(event);

        // Then
        verify(eventRepository).save(event);

        ArgumentCaptor<EventAudit> auditCaptor =
                ArgumentCaptor.forClass(EventAudit.class);

        verify(eventAuditRepository).save(auditCaptor.capture());

        EventAudit audit = auditCaptor.getValue();

        assertSame(event, audit.getEvent());
        assertNotNull(audit.getAuditedAt());
    }
    @Test
    void shouldRejectDuplicateQualityEventWithoutSavingEventOrAudit() {
        // Given: el identificador ya está registrado
        Event event = new Event(
                "EVT-1002",
                Instant.parse("2026-10-28T14:00:00Z"),
                EventType.QUALITY,
                "Control de calidad aprobado"
        );

        when(eventRepository.existsByEventId(event.getEventId()))
                .thenReturn(true);

        // When / Then
        assertThrows(
                DuplicateEventException.class,
                () -> eventService.register(event)
        );

        verify(eventRepository, never()).save(any(Event.class));
        verifyNoInteractions(eventAuditRepository);
    }
}
