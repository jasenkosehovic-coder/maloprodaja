import {
  Directive,
  TemplateRef,
  ViewContainerRef,
  effect,
  inject,
  input,
} from '@angular/core';
import { AuthService } from '../../core/auth/services/auth.service';
import { Uloga } from '../../core/auth/models/auth.models';

/**
 * Structural directive that conditionally renders content based on user roles.
 * Uses effect() to reactively track role changes.
 *
 * Usage:
 *   <button *appHasRole="['ADMIN', 'SUPER_ADMIN']">Admin only</button>
 */
@Directive({
  selector: '[appHasRole]',
  standalone: true,
})
export class HasRoleDirective {
  private readonly templateRef = inject(TemplateRef<unknown>);
  private readonly viewContainerRef = inject(ViewContainerRef);
  private readonly authService = inject(AuthService);

  appHasRole = input.required<Uloga[]>();

  constructor() {
    effect(() => {
      const allowed = this.appHasRole();
      if (this.authService.hasRole(...allowed)) {
        if (!this.viewContainerRef.length) {
          this.viewContainerRef.createEmbeddedView(this.templateRef);
        }
      } else {
        this.viewContainerRef.clear();
      }
    });
  }
}
