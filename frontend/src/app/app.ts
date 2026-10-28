import { ChangeDetectionStrategy, Component } from '@angular/core';
import { EventsPage } from './events/events-page';

@Component({
  selector: 'app-root',
  imports: [EventsPage],
  templateUrl: './app.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {}
