import {
  Directive,
  OnInit,
  TemplateRef,
  ViewContainerRef,
  inject,
  input,
} from '@angular/core';
import { AuthService } from '../../core/auth/services/auth.service';
import { Uloga } from '../../core/auth/models/auth.models';

/**
 * Structural directive that conditionally renders content based on user roles.
 *
 * Usage:
 *   <button *appHasRole="['ADMIN', 'SUPER_ADMIN']">Admin only</button>
 */
@Directive({
  selector: '[appHasRole]',
  standalone: true,
})
export class HasRoleDirective implements OnInit {
  private readonly templateRef = inject(TemplateRef<unknown>);
  private readonly viewContainerRef = inject(ViewContainerRef);
  private readonly authService = inject(AuthService);

  appHasRole = input.required<Uloga[]>();

  ngOnInit(): void {
    const allowed = this.appHasRole();
    if (this.authService.hasRole(...allowed)) {
      this.viewContainerRef.createEmbeddedView(this.templateRef);
    } else {
      this.viewContainerRef.clear();
    }
  }
}
