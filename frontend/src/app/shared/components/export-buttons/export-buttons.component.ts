import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';

export type ExportFormat = 'excel' | 'csv' | 'pdf';

@Component({
  selector: 'app-export-buttons',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatTooltipModule, MatMenuModule],
  template: `
    <button
      mat-stroked-button
      [matMenuTriggerFor]="exportMenu"
      aria-label="Opcije za izvoz podataka"
    >
      <mat-icon>file_download</mat-icon>
      Izvoz
    </button>

    <mat-menu #exportMenu="matMenu">
      @if (showExcel()) {
        <button mat-menu-item (click)="onExport('excel')">
          <mat-icon>table_chart</mat-icon>
          <span>Excel (.xlsx)</span>
        </button>
      }
      @if (showCsv()) {
        <button mat-menu-item (click)="onExport('csv')">
          <mat-icon>text_snippet</mat-icon>
          <span>CSV</span>
        </button>
      }
      @if (showPdf()) {
        <button mat-menu-item (click)="onExport('pdf')">
          <mat-icon>picture_as_pdf</mat-icon>
          <span>PDF</span>
        </button>
      }
    </mat-menu>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExportButtonsComponent {
  showExcel = input(true);
  showCsv = input(true);
  showPdf = input(true);

  exportClick = output<ExportFormat>();

  onExport(format: ExportFormat): void {
    this.exportClick.emit(format);
  }
}
