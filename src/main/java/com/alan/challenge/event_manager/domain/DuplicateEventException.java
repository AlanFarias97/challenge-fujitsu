package com.alan.challenge.event_manager.domain;

public class DuplicateEventException extends RuntimeException {

    public DuplicateEventException(String eventId) {
        super("El evento " + eventId + " ya fue registrado");
    }
}