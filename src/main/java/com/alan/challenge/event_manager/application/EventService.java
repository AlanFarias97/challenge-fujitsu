package com.alan.challenge.event_manager.application;

import com.alan.challenge.event_manager.domain.Event;
import com.alan.challenge.event_manager.persistence.EventAuditRepository;
import com.alan.challenge.event_manager.persistence.EventRepository;

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
        eventRepository.save(event);
    }
}