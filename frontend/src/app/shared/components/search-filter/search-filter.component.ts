import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-search-filter',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
  ],
  template: `
    <mat-form-field appearance="outline" class="search-field">
      <mat-label>{{ placeholder() }}</mat-label>
      <mat-icon matPrefix>search</mat-icon>
      <input
        matInput
        [formControl]="searchControl"
        [attr.aria-label]="placeholder()"
      />
      @if (searchControl.value) {
        <button
          mat-icon-button
          matSuffix
          (click)="clearSearch()"
          aria-label="Obriši pretragu"
        >
          <mat-icon>close</mat-icon>
        </button>
      }
    </mat-form-field>
  `,
  styles: [`
    .search-field {
      width: 100%;
      max-width: 400px;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SearchFilterComponent {
  private readonly fb = inject(NonNullableFormBuilder);

  placeholder = input('Pretraži...');
  debounce = input(300);

  searchChange = output<string>();

  searchControl = this.fb.control('');

  constructor() {
    this.searchControl.valueChanges.pipe(
      debounceTime(this.debounce()),
      distinctUntilChanged(),
      takeUntilDestroyed()
    ).subscribe((value) => {
      this.searchChange.emit(value);
    });
  }

  clearSearch(): void {
    this.searchControl.setValue('');
  }
}
