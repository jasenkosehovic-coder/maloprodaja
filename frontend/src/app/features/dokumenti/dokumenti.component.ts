import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-dokumenti',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './dokumenti.component.html',
  styleUrl: './dokumenti.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DokumentiComponent {}
