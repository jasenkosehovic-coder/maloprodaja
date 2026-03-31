import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-sifarnici-shell',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './sifarnici-shell.component.html',
  styleUrl: './sifarnici-shell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SifarniciShellComponent {}
