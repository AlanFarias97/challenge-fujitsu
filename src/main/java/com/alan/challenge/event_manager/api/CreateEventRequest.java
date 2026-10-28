package com.alan.challenge.event_manager.api;

import com.alan.challenge.event_manager.domain.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateEventRequest(

        @NotBlank(message = "El identificador es obligatorio")
        @Size(max = 100, message = "El identificador admite hasta 100 caracteres")
        String eventId,

        @NotNull(message = "La fecha del evento es obligatoria")
        Instant timestamp,

        @NotNull(message = "El tipo de evento es obligatorio")
        EventType eventType,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 2000, message = "La descripción admite hasta 2000 caracteres")
        String description
) {
}