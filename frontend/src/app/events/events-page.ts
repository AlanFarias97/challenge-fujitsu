import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription } from 'rxjs';
import { EventApiService } from './event-api.service';
import { apiErrorMessage } from './api-error';
import { EventForm } from './event-form';
import { EventList } from './event-list';
import { CreateEventRequest, EventRecord, Feedback } from './event.model';

@Component({
  selector: 'app-events-page',
  imports: [DatePipe, EventForm, EventList],
  templateUrl: './events-page.html',
  styleUrl: './events-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventsPage implements OnInit {
  private readonly api = inject(EventApiService);
  private readonly destroyRef = inject(DestroyRef);
  private listSubscription?: Subscription;
  readonly events = signal<EventRecord[]>([]);
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly loadError = signal<string | null>(null);
  readonly feedback = signal<Feedback | null>(null);
  readonly selectedEvent = signal<EventRecord | null>(null);
  readonly lastUpdated = signal<Date | null>(null);
  readonly productionCount = computed(
    () => this.events().filter((e) => e.eventType === 'PRODUCTION').length,
  );
  readonly qualityCount = computed(
    () => this.events().filter((e) => e.eventType === 'QUALITY').length,
  );

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.listSubscription?.unsubscribe();
    this.loading.set(true);
    this.loadError.set(null);
    this.listSubscription = this.api
      .list()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (events) => {
          this.events.set(this.sortEvents(events));
          this.lastUpdated.set(new Date());
          this.loading.set(false);
        },
        error: (error: HttpErrorResponse) => {
          this.loadError.set(
            apiErrorMessage(error, 'No se pudo consultar el historial de eventos.'),
          );
          this.loading.set(false);
        },
      });
  }

  register(event: CreateEventRequest): void {
    if (this.submitting()) return;
    this.submitting.set(true);
    this.feedback.set(null);
    this.api
      .register(event)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (saved) => {
          this.submitting.set(false);
          this.feedback.set({
            kind: 'success',
            title: 'Evento registrado',
            detail:
              saved.eventType === 'QUALITY'
                ? `${saved.eventId} se registró y se generó su auditoría de calidad.`
                : `${saved.eventId} se registró correctamente.`,
          });
          this.events.update((events) =>
            this.sortEvents([...events.filter((e) => e.id !== saved.id), saved]),
          );
          this.loadEvents();
        },
        error: (error: HttpErrorResponse) => {
          this.submitting.set(false);
          this.feedback.set({
            kind: error.status === 409 ? 'warning' : 'error',
            title:
              error.status === 409
                ? 'Este evento ya está registrado'
                : 'No se pudo registrar el evento',
            detail: apiErrorMessage(
              error,
              'No se confirmó el registro. Podés reintentar con el mismo identificador; si ya se guardó, el servidor lo reconocerá como duplicado.',
            ),
          });
        },
      });
  }

  reuseEvent(event: EventRecord): void {
    if (this.submitting()) return;
    this.selectedEvent.set({ ...event });
    this.feedback.set(null);
    document.getElementById('registro')?.scrollIntoView({ block: 'start' });
    document.getElementById('event-id')?.focus({ preventScroll: true });
  }

  private sortEvents(events: EventRecord[]): EventRecord[] {
    return [...events].sort((a, b) => b.id - a.id);
  }
}
