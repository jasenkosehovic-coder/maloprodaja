import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { AbstractControl } from '@angular/forms';

@Component({
  selector: 'app-form-field-error',
  standalone: true,
  template: `
    @if (control() && control()!.invalid && (control()!.dirty || control()!.touched)) {
      <div class="field-error" role="alert" aria-live="polite">
        @if (control()!.hasError('required')) {
          <span>{{ label() }} je obavezno polje.</span>
        } @else if (control()!.hasError('email')) {
          <span>Unesite ispravnu email adresu.</span>
        } @else if (control()!.hasError('minlength')) {
          <span>
            {{ label() }} mora imati najmanje
            {{ control()!.getError('minlength').requiredLength }} znakova.
          </span>
        } @else if (control()!.hasError('maxlength')) {
          <span>
            {{ label() }} ne smije biti duži od
            {{ control()!.getError('maxlength').requiredLength }} znakova.
          </span>
        } @else if (control()!.hasError('pattern')) {
          <span>{{ label() }} nije u ispravnom formatu.</span>
        } @else if (control()!.hasError('min')) {
          <span>
            Minimalna vrijednost je {{ control()!.getError('min').min }}.
          </span>
        } @else if (control()!.hasError('max')) {
          <span>
            Maksimalna vrijednost je {{ control()!.getError('max').max }}.
          </span>
        } @else if (customMessage()) {
          <span>{{ customMessage() }}</span>
        }
      </div>
    }
  `,
  styles: [`
    .field-error {
      color: #d32f2f;
      font-size: 12px;
      margin-top: 4px;
      display: flex;
      align-items: center;
      gap: 4px;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FormFieldErrorComponent {
  control = input<AbstractControl | null>(null);
  label = input.required<string>();
  customMessage = input('');
}
