import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CreateEventRequest, EventRecord } from './event.model';

@Injectable({ providedIn: 'root' })
export class EventApiService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/events';

  list() {
    return this.http.get<EventRecord[]>(this.url);
  }

  register(event: CreateEventRequest) {
    return this.http.post<EventRecord>(this.url, event);
  }
}
