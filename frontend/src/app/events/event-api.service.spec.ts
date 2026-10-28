import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { beforeEach, afterEach, describe, expect, it } from 'vitest';
import { EventApiService } from './event-api.service';

describe('EventApiService', () => {
  let api: EventApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(EventApiService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('consulta eventos mediante el proxy', () => {
    api.list().subscribe((events) => expect(events).toEqual([]));
    const request = http.expectOne('/api/events');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('envía el eventId original y los campos que espera el backend', () => {
    const event = {
      eventId: 'EVT-1001',
      eventType: 'QUALITY' as const,
      timestamp: '2026-10-28T13:00:00Z',
      description: 'Control aprobado',
    };
    api.register(event).subscribe((saved) => expect(saved.id).toBe(1));
    const request = http.expectOne('/api/events');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(event);
    request.flush({ ...event, id: 1 }, { status: 201, statusText: 'Created' });
  });
});
