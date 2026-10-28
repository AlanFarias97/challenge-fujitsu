package com.alan.challenge.event_manager.application;

import com.alan.challenge.event_manager.domain.Event;
import com.alan.challenge.event_manager.domain.EventType;
import com.alan.challenge.event_manager.persistence.EventAuditRepository;
import com.alan.challenge.event_manager.persistence.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
}
