import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { InactivityService } from './core/auth/services/inactivity.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet />`,
})
export class AppComponent {
  // Injecting InactivityService here ensures it is instantiated at app startup
  // so its effect() — which starts/stops monitoring based on isLoggedIn — is active
  // for the full lifetime of the application.
  private readonly _inactivityService = inject(InactivityService);
}
