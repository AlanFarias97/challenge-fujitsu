import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, afterEach, describe, expect, it } from 'vitest';
import { EventsPage } from './events-page';
import { EventRecord } from './event.model';

describe('EventsPage', () => {
  let fixture: ComponentFixture<EventsPage>;
  let http: HttpTestingController;
  const event: EventRecord = {
    id: 1,
    eventId: 'EVT-1001',
    eventType: 'QUALITY',
    timestamp: '2026-10-28T13:00:00Z',
    description: 'Control aprobado',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [EventsPage],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(EventsPage);
    fixture.detectChanges();
  });
  afterEach(() => http.verify());

  it('muestra el historial y los contadores consultados', () => {
    http.expectOne('/api/events').flush([event]);
    fixture.detectChanges();
    expect(fixture.componentInstance.qualityCount()).toBe(1);
    expect(fixture.componentInstance.productionCount()).toBe(0);
    expect(fixture.nativeElement.querySelector('tbody').textContent).toContain('EVT-1001');
  });

  it('muestra éxito y actualiza la tabla después del registro', () => {
    http.expectOne('/api/events').flush([]);
    fixture.componentInstance.register(event);
    http.expectOne('/api/events').flush(event, { status: 201, statusText: 'Created' });
    http.expectOne('/api/events').flush([event]);
    fixture.detectChanges();
    expect(fixture.componentInstance.feedback()?.kind).toBe('success');
    expect(fixture.componentInstance.submitting()).toBe(false);
    expect(fixture.componentInstance.events()).toHaveLength(1);
  });

  it('muestra el mensaje de duplicado sin agregar otra fila', () => {
    http.expectOne('/api/events').flush([event]);
    fixture.componentInstance.register(event);
    http
      .expectOne('/api/events')
      .flush(
        { detail: 'El evento EVT-1001 ya fue registrado' },
        { status: 409, statusText: 'Conflict' },
      );
    fixture.detectChanges();
    expect(fixture.componentInstance.feedback()?.kind).toBe('warning');
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'El evento EVT-1001 ya fue registrado',
    );
    expect(fixture.componentInstance.events()).toHaveLength(1);
    expect(fixture.componentInstance.submitting()).toBe(false);
  });

  it('muestra los detalles de validación enviados por el servidor', () => {
    http.expectOne('/api/events').flush([]);
    fixture.componentInstance.register(event);
    http
      .expectOne('/api/events')
      .flush(
        {
          detail: 'Revisá los campos enviados',
          errors: ['description: La descripción es obligatoria'],
        },
        { status: 400, statusText: 'Bad Request' },
      );
    expect(fixture.componentInstance.feedback()?.detail).toContain('La descripción es obligatoria');
    expect(fixture.componentInstance.events()).toEqual([]);
  });

  it('permite recuperarse de un error al consultar sin mostrar datos inventados', () => {
    http.expectOne('/api/events').error(new ProgressEvent('error'));
    expect(fixture.componentInstance.loadError()).toContain('No pudimos conectar');
    expect(fixture.componentInstance.events()).toEqual([]);
    fixture.componentInstance.loadEvents();
    http.expectOne('/api/events').flush([event]);
    expect(fixture.componentInstance.loadError()).toBeNull();
    expect(fixture.componentInstance.events()).toHaveLength(1);
  });

  it('conserva el éxito y el registro creado si falla la actualización posterior', () => {
    http.expectOne('/api/events').flush([]);
    fixture.componentInstance.register(event);
    http.expectOne('/api/events').flush(event, { status: 201, statusText: 'Created' });
    http.expectOne('/api/events').error(new ProgressEvent('error'));
    expect(fixture.componentInstance.feedback()?.kind).toBe('success');
    expect(fixture.componentInstance.events()).toHaveLength(1);
    expect(fixture.componentInstance.loadError()).not.toBeNull();
  });
});
