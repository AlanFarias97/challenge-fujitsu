import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { EventRecord, EventType } from './event.model';

@Component({
  selector: 'app-event-list',
  imports: [DatePipe],
  templateUrl: './event-list.html',
  styleUrl: './event-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventList {
  readonly events = input.required<EventRecord[]>();
  readonly loading = input(false);
  readonly error = input<string | null>(null);
  readonly submitting = input(false);
  readonly refreshed = output<void>();
  readonly reused = output<EventRecord>();
  readonly query = signal('');
  readonly type = signal('ALL');

  readonly filteredEvents = computed(() => {
    const query = this.query().trim().toLocaleLowerCase();
    return this.events().filter(
      (event) =>
        (this.type() === 'ALL' || event.eventType === this.type()) &&
        (!query || `${event.eventId} ${event.description}`.toLocaleLowerCase().includes(query)),
    );
  });

  readonly typeLabels: Record<EventType, string> = { PRODUCTION: 'Producción', QUALITY: 'Calidad' };
}
