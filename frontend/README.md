# Event Manager · Frontend

Aplicación Angular 21 standalone para registrar y consultar eventos de fábrica.

## Ejecución local

Requisitos: Node.js 20.19 o superior dentro de la rama 20 (también se admiten versiones compatibles de Node 22 y 24), npm y el backend ejecutándose en el puerto 8080.

Desde esta carpeta:

```shell
npm ci
npm start
```

Si npm 10 falla al instalar con `Cannot read properties of null (reading 'edgesOut')`, usar temporalmente un instalador más nuevo, sin cambiar npm global:

```shell
npm exec --yes --package=npm@11.6.2 -- npm ci
```

Abrir http://127.0.0.1:4200. Desde la carpeta del backend se puede iniciar Spring Boot con `./mvnw spring-boot:run` o `.\mvnw.cmd spring-boot:run` en Windows.

## API y proxy

El frontend utiliza `GET /api/events` y `POST /api/events`. El servidor de desarrollo reenvía `/api/**` al backend en http://localhost:8080 y elimina el prefijo `/api` (ver `proxy.conf.json`). No es necesario cambiar CORS en Java. Si cambia el puerto del backend, ajustar el destino y reiniciar `npm start`.

El proxy es exclusivo del servidor de desarrollo. Al desplegar el build se debe configurar el servidor web para reenviar `/api/events` a `/events` en el backend.

## Funcionalidad

- Registro de PRODUCTION y QUALITY con validaciones de campos y longitudes alineadas con la API.
- Fecha/hora local editable; se convierte a UTC ISO 8601 antes del envío.
- El eventId se introduce manualmente y no se normaliza ni se regenera. Los datos permanecen en el formulario después del envío para probar duplicados.
- Tabla con actualización manual, búsqueda, filtro por tipo y acción para reutilizar un evento en el formulario.
- Mensajes de éxito, errores de validación, duplicados HTTP 409 y problemas de conexión.
- Los contadores muestran eventos consultados, no el número de auditorías.

## Probar un duplicado y consultar auditorías

1. Registrar un QUALITY con un eventId nuevo y una descripción.
2. Sin limpiar el formulario, volver a presionar Registrar evento.
3. La API debe responder 409 y la interfaz debe mostrar el mensaje de duplicado.
4. Actualizar el historial: el evento debe aparecer una sola vez.
5. La auditoría se verifica en la consola H2 del backend, http://localhost:8080/h2-console. Usar la URL JDBC y credenciales de su configuración.

```sql
SELECT e.event_id, e.event_type, a.id AS audit_id, a.audited_at
FROM events e
LEFT JOIN event_audits a ON a.event_id = e.id;
```

PRODUCTION no genera auditoría. QUALITY genera una sola, incluso al reenviarlo.

## Organización

`events-page` coordina solicitudes y estado; `event-form` contiene el formulario reactivo; `event-list` muestra y filtra los registros; `event-api.service` concentra HTTP. Los contratos están en `event.model`. Se utilizan signals y componentes con OnPush; no se agrega una biblioteca de estado para este alcance.

## Verificación

```shell
npm test
npm run build
```

Los tests de HTTP utilizan el controlador de pruebas de Angular, no escriben en H2. Los tests del formulario comprueban validación, conversión de fecha y conservación del identificador.

## Mejoras futuras

Paginación desde backend, filtros del lado del servidor y tests de extremo a extremo en CI. Las fuentes web tienen alternativas del sistema si no hay conexión.
