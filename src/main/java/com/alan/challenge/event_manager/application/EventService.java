package com.alan.challenge.event_manager.application;

import com.alan.challenge.event_manager.domain.DuplicateEventException;
import com.alan.challenge.event_manager.domain.Event;
import com.alan.challenge.event_manager.domain.EventAudit;
import com.alan.challenge.event_manager.domain.EventType;
import com.alan.challenge.event_manager.persistence.EventAuditRepository;
import com.alan.challenge.event_manager.persistence.EventRepository;

import java.time.Instant;

public class EventService {

    private final EventRepository eventRepository;
    private final EventAuditRepository eventAuditRepository;

    public EventService(
            EventRepository eventRepository,
            EventAuditRepository eventAuditRepository
    ) {
        this.eventRepository = eventRepository;
        this.eventAuditRepository = eventAuditRepository;
    }

    public void register(Event event) {
        if (eventRepository.existsByEventId(event.getEventId())) {
            throw new DuplicateEventException(event.getEventId());
        }

        Event savedEvent = eventRepository.save(event);

        if (event.getEventType() == EventType.QUALITY) {
            EventAudit audit = new EventAudit(savedEvent, Instant.now());
            eventAuditRepository.save(audit);
        }
    }

}