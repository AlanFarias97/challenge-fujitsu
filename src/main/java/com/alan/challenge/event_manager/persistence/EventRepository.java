package com.alan.challenge.event_manager.persistence;

import com.alan.challenge.event_manager.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByEventId(String eventId);
}