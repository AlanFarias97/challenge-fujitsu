package com.alan.challenge.event_manager.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "event_audits")
public class EventAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "event_id",
            nullable = false,
            unique = true
    )
    private Event event;

    @Column(name = "audited_at", nullable = false)
    private Instant auditedAt;

    protected EventAudit() {
    }

    public EventAudit(Event event, Instant auditedAt) {
        this.event = event;
        this.auditedAt = auditedAt;
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public Instant getAuditedAt() {
        return auditedAt;
    }
}