-- Consultas de lectura para la consola H2 del backend.
-- URL: jdbc:h2:file:./data/event-manager | usuario: sa | contraseña vacía.
-- Si se cambiaron los identificadores en events.http, ajustar estos filtros.

-- 1. Eventos de la demo y cantidad de auditorías.
-- Esperado: DEMO-PRODUCTION-001 -> 0, DEMO-QUALITY-001 -> 1.
-- El resultado debe mantenerse después de reenviar QUALITY.
SELECT
    e.event_id,
    e.event_type,
    COUNT(a.id) AS audit_count
FROM events e
LEFT JOIN event_audits a ON a.event_id = e.id
WHERE e.event_id IN ('DEMO-PRODUCTION-001', 'DEMO-QUALITY-001')
GROUP BY e.event_id, e.event_type
ORDER BY e.event_id;

-- 2. Detalle del vínculo entre el ID interno, la identidad externa y la auditoría.
SELECT
    e.id AS internal_event_id,
    e.event_id AS external_event_id,
    e.event_type,
    e.event_timestamp,
    a.id AS audit_id,
    a.audited_at
FROM events e
LEFT JOIN event_audits a ON a.event_id = e.id
WHERE e.event_id IN ('DEMO-PRODUCTION-001', 'DEMO-QUALITY-001')
ORDER BY e.event_id;

-- 3. Los requests inválidos de la demo no deben haber creado eventos.
-- Esperado: 0.
SELECT COUNT(*) AS invalid_events_saved
FROM events
WHERE event_id IN ('DEMO-INVALID-001', 'DEMO-INVALID-002');

-- 4. Comprobación general de identidades repetidas. Esperado: ninguna fila.
SELECT event_id, COUNT(*) AS event_count
FROM events
GROUP BY event_id
HAVING COUNT(*) > 1;
