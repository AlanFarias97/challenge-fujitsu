import { ChangeDetectionStrategy, Component, effect, inject, input, output } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { CreateEventRequest, EventRecord, EventType } from './event.model';

export function toLocalDateTime(iso = new Date().toISOString()): string {
  const date = new Date(iso);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60_000).toISOString().slice(0, 19);
}

function validDate(control: AbstractControl): ValidationErrors | null {
  return control.value && Number.isNaN(new Date(control.value).getTime())
    ? { invalidDate: true }
    : null;
}

@Component({
  selector: 'app-event-form',
  imports: [ReactiveFormsModule],
  templateUrl: './event-form.html',
  styleUrl: './event-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventForm {
  readonly busy = input(false);
  readonly selectedEvent = input<EventRecord | null>(null);
  readonly submitted = output<CreateEventRequest>();
  private readonly fb = inject(FormBuilder);
  readonly timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

  readonly form = this.fb.nonNullable.group({
    eventId: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    eventType: this.fb.nonNullable.control<EventType>('PRODUCTION', Validators.required),
    timestamp: [toLocalDateTime(), [Validators.required, validDate]],
    description: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(2000)]],
  });

  constructor() {
    effect(() => {
      const event = this.selectedEvent();
      if (!event) return;
      this.form.reset({
        eventId: event.eventId,
        eventType: event.eventType,
        timestamp: toLocalDateTime(event.timestamp),
        description: event.description,
      });
    });
  }

  invalid(field: 'eventId' | 'timestamp' | 'description'): boolean {
    const control = this.form.controls[field];
    return control.invalid && control.touched;
  }

  submit(): void {
    if (this.busy()) return;
    this.form.markAllAsTouched();
    if (this.form.invalid) return;

    const value = this.form.getRawValue();
    this.submitted.emit({ ...value, timestamp: new Date(value.timestamp).toISOString() });
    // Se conservan los campos para permitir el reenvío del mismo evento.
  }

  reset(): void {
    if (this.busy()) return;
    this.form.reset({
      eventId: '',
      eventType: 'PRODUCTION',
      timestamp: toLocalDateTime(),
      description: '',
    });
  }
}
