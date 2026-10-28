import { TestBed } from '@angular/core/testing';
import { describe, beforeEach, expect, it, vi } from 'vitest';
import { EventForm, toLocalDateTime } from './event-form';
import { EventRecord } from './event.model';

describe('EventForm', () => {
  beforeEach(() => TestBed.configureTestingModule({ imports: [EventForm] }));

  it('rechaza campos vacíos y muestra los errores', () => {
    const fixture = TestBed.createComponent(EventForm);
    const submitted = vi.fn();
    fixture.componentInstance.submitted.subscribe(submitted);
    fixture.componentInstance.submit();
    fixture.detectChanges();
    expect(submitted).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('#event-id-error')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('#description-error')).not.toBeNull();
  });

  it('rechaza identificadores o descripciones compuestos solo por espacios', () => {
    const fixture = TestBed.createComponent(EventForm);
    fixture.componentInstance.form.patchValue({ eventId: '   ', description: '   ' });
    expect(fixture.componentInstance.form.invalid).toBe(true);
  });

  it('convierte la fecha a UTC y conserva los datos para reenviar el mismo evento', () => {
    const fixture = TestBed.createComponent(EventForm);
    const form = fixture.componentInstance;
    const submitted = vi.fn();
    form.submitted.subscribe(submitted);
    const localTime = '2026-10-28T10:00:00';
    form.form.setValue({
      eventId: ' EVT-1001 ',
      description: 'Control aprobado',
      eventType: 'QUALITY',
      timestamp: localTime,
    });
    form.submit();
    form.submit();
    const expected = {
      eventId: ' EVT-1001 ',
      description: 'Control aprobado',
      eventType: 'QUALITY',
      timestamp: new Date(localTime).toISOString(),
    };
    expect(submitted).toHaveBeenCalledTimes(2);
    expect(submitted.mock.calls[0][0]).toEqual(expected);
    expect(submitted.mock.calls[1][0]).toEqual(expected);
    expect(form.form.controls.eventId.value).toBe(' EVT-1001 ');
    expect(toLocalDateTime(expected.timestamp)).toBe(localTime);
  });

  it('no permite enviar nuevamente mientras hay una solicitud pendiente', () => {
    const fixture = TestBed.createComponent(EventForm);
    const submitted = vi.fn();
    fixture.componentInstance.submitted.subscribe(submitted);
    fixture.componentInstance.form.patchValue({
      eventId: 'EVT-1001',
      description: 'Evento válido',
    });
    fixture.componentRef.setInput('busy', true);
    fixture.componentInstance.submit();
    expect(submitted).not.toHaveBeenCalled();
  });

  it('carga los datos originales de un evento seleccionado', () => {
    const fixture = TestBed.createComponent(EventForm);
    const event: EventRecord = {
      id: 1,
      eventId: 'EVT-1001',
      description: 'Control aprobado',
      eventType: 'QUALITY',
      timestamp: '2026-10-28T13:00:00Z',
    };
    fixture.componentRef.setInput('selectedEvent', event);
    fixture.detectChanges();
    expect(fixture.componentInstance.form.getRawValue()).toEqual({
      eventId: event.eventId,
      eventType: event.eventType,
      description: event.description,
      timestamp: toLocalDateTime(event.timestamp),
    });
  });
});
