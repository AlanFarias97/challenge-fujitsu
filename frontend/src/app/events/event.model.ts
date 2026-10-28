export type EventType = 'PRODUCTION' | 'QUALITY';

export interface CreateEventRequest {
  eventId: string;
  timestamp: string;
  eventType: EventType;
  description: string;
}

export interface EventRecord extends CreateEventRequest {
  id: number;
}

export interface Feedback {
  kind: 'success' | 'warning' | 'error';
  title: string;
  detail: string;
}
