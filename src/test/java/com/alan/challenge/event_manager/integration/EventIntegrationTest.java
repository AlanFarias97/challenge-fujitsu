package com.alan.challenge.event_manager.integration;

import com.alan.challenge.event_manager.application.EventService;
import com.alan.challenge.event_manager.domain.Event;
import com.alan.challenge.event_manager.domain.EventAudit;
import com.alan.challenge.event_manager.domain.EventType;
import com.alan.challenge.event_manager.persistence.EventAuditRepository;
import com.alan.challenge.event_manager.persistence.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventService eventService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private EventRepository eventRepository;

    @MockitoSpyBean
    private EventAuditRepository eventAuditRepository;

    @BeforeEach
    void cleanDatabase() {
        eventAuditRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {"PRODUCTION", "QUALITY"})
    void shouldRegisterEventAndCreateAuditOnlyForQuality(
            String eventType
    ) throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(
                                "EVT-1001",
                                eventType,
                                "Evento de prueba",
                                "2026-10-28T13:00:00Z"
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.eventId").value("EVT-1001"))
                .andExpect(jsonPath("$.eventType").value(eventType));

        assertEquals(1L, eventRepository.count());

        if ("QUALITY".equals(eventType)) {
            assertEquals(1L, eventAuditRepository.count());

            Long eventInternalId = jdbcTemplate.queryForObject(
                    "select id from events where event_id = ?",
                    Long.class,
                    "EVT-1001"
            );

            Long auditedEventId = jdbcTemplate.queryForObject(
                    "select event_id from event_audits",
                    Long.class
            );

            assertEquals(eventInternalId, auditedEventId);

            assertNotNull(jdbcTemplate.queryForObject(
                    "select audited_at from event_audits",
                    Object.class
            ));
        } else {
            assertEquals(0L, eventAuditRepository.count());
        }

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventId").value("EVT-1001"));
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoEvents() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldRejectDuplicateWithoutCreatingAnotherAudit() throws Exception {
        String request = payload(
                "EVT-DUP",
                "QUALITY",
                "Control aprobado",
                "2026-10-28T13:00:00Z"
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Evento duplicado"))
                .andExpect(jsonPath("$.detail")
                        .value("El evento EVT-DUP ya fue registrado"));

        assertEquals(1L, eventRepository.count());
        assertEquals(1L, eventAuditRepository.count());
    }

    @Test
    void shouldReturn409WhenDatabaseDetectsDuplicate() throws Exception {
        String request = payload(
                "EVT-RACE",
                "QUALITY",
                "Control aprobado",
                "2026-10-28T13:00:00Z"
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        // Simulamos que la consulta previa no detectó el duplicado.
        // El guardado y la restricción de H2 siguen siendo reales.
        doReturn(false)
                .when(eventRepository)
                .existsByEventId("EVT-RACE");

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Evento duplicado"));

        assertEquals(1L, eventRepository.count());
        assertEquals(1L, eventAuditRepository.count());
    }

    @Test
    void shouldRollbackEventWhenAuditFails() {
        Event event = new Event(
                "EVT-ROLLBACK",
                Instant.parse("2026-10-28T13:00:00Z"),
                EventType.QUALITY,
                "Control aprobado"
        );

        doThrow(new IllegalStateException("Fallo de auditoría"))
                .when(eventAuditRepository)
                .save(any(EventAudit.class));

        assertThrows(
                IllegalStateException.class,
                () -> eventService.register(event)
        );

        assertFalse(eventRepository.existsByEventId("EVT-ROLLBACK"));
        assertEquals(0L, eventRepository.count());
        assertEquals(0L, eventAuditRepository.count());
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void shouldRejectInvalidRequestsWithoutSavingData(
            String request
    ) throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").isNotEmpty());

        assertEquals(0L, eventRepository.count());
        assertEquals(0L, eventAuditRepository.count());
    }

    @Test
    void shouldAcceptOnlyOneOfTwoConcurrentRequests() throws Exception {
        String request = payload(
                "EVT-CONCURRENT",
                "QUALITY",
                "Control aprobado",
                "2026-10-28T13:00:00Z"
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<Integer>> requests = Stream.of(1, 2)
                    .map(ignored -> executor.submit(() -> {
                        if (!start.await(10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException(
                                    "No se inició la prueba concurrente"
                            );
                        }

                        return mockMvc.perform(post("/events")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(request))
                                .andReturn()
                                .getResponse()
                                .getStatus();
                    }))
                    .toList();

            start.countDown();

            List<Integer> statuses = List.of(
                    requests.get(0).get(15, TimeUnit.SECONDS),
                    requests.get(1).get(15, TimeUnit.SECONDS)
            );

            assertEquals(
                    List.of(201, 409),
                    statuses.stream().sorted().toList()
            );

            assertEquals(1L, eventRepository.count());
            assertEquals(1L, eventAuditRepository.count());
        } finally {
            executor.shutdownNow();
        }
    }

    private static Stream<String> invalidRequests() {
        return Stream.of(
                "{}",
                "{",
                payload("", "QUALITY", "Descripción",
                        "2026-10-28T13:00:00Z"),
                payload("x".repeat(101), "QUALITY", "Descripción",
                        "2026-10-28T13:00:00Z"),
                payload("EVT-INVALID", "QUALITY", "",
                        "2026-10-28T13:00:00Z"),
                payload("EVT-INVALID", "QUALITY", "x".repeat(2001),
                        "2026-10-28T13:00:00Z"),
                payload("EVT-INVALID", "UNKNOWN", "Descripción",
                        "2026-10-28T13:00:00Z"),
                payload("EVT-INVALID", "QUALITY", "Descripción",
                        "fecha-inválida")
        );
    }

    private static String payload(
            String eventId,
            String eventType,
            String description,
            String timestamp
    ) {
        return """
                {
                  "eventId": "%s",
                  "eventType": "%s",
                  "description": "%s",
                  "timestamp": "%s"
                }
                """.formatted(eventId, eventType, description, timestamp);
    }
}