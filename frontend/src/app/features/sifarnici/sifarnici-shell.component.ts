import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';

@Component({
  selector: 'app-sifarnici-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatTabsModule],
  template: `
    <nav mat-tab-nav-bar [tabPanel]="tabPanel" aria-label="Šifarnici navigacija">
      <a mat-tab-link routerLink="artikli" routerLinkActive #rla1="routerLinkActive" [active]="rla1.isActive">Artikli</a>
      <a mat-tab-link routerLink="grupe" routerLinkActive #rla2="routerLinkActive" [active]="rla2.isActive">Grupe</a>
      <a mat-tab-link routerLink="dobavljaci" routerLinkActive #rla3="routerLinkActive" [active]="rla3.isActive">Dobavljači</a>
      <a mat-tab-link routerLink="kupci" routerLinkActive #rla4="routerLinkActive" [active]="rla4.isActive">Kupci</a>
    </nav>
    <mat-tab-nav-panel #tabPanel>
      <router-outlet />
    </mat-tab-nav-panel>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SifarniciShellComponent {}
