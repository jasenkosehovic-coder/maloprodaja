import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';

@Component({
  selector: 'app-artikli-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatTabsModule],
  templateUrl: './artikli-shell.component.html',
  styleUrl: './artikli-shell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArtikliShellComponent {}
