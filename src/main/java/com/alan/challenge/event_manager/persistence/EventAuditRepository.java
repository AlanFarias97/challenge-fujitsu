package com.alan.challenge.event_manager.persistence;

import com.alan.challenge.event_manager.domain.EventAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAuditRepository extends JpaRepository<EventAudit, Long> {
}