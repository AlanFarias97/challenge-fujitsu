package com.alan.challenge.event_manager.api;

import com.alan.challenge.event_manager.application.EventService;
import com.alan.challenge.event_manager.domain.Event;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    private static final Logger log =
            LoggerFactory.getLogger(EventController.class);

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> register(
            @Valid @RequestBody CreateEventRequest request
    ) {
        Event event = new Event(
                request.eventId(),
                request.timestamp(),
                request.eventType(),
                request.description()
        );

        Event savedEvent = eventService.register(event);

        log.info(
                "Evento registrado: eventId={}, eventType={}",
                savedEvent.getEventId(),
                savedEvent.getEventType()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EventResponse.from(savedEvent));
    }

    @GetMapping
    public List<EventResponse> findAll() {
        return eventService.findAll()
                .stream()
                .map(EventResponse::from)
                .toList();
    }
}