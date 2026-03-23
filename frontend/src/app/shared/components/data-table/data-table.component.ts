import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output,
} from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';

export interface TableColumn<T = Record<string, unknown>> {
  key: keyof T & string;
  label: string;
  sortable?: boolean;
  type?: 'text' | 'number' | 'date' | 'boolean' | 'currency' | 'custom';
  width?: string;
  align?: 'left' | 'center' | 'right';
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatProgressBarModule,
  ],
  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DataTableComponent<T extends Record<string, unknown>> {
  data = input.required<T[]>();
  columns = input.required<TableColumn<T>[]>();
  isLoading = input(false);
  totalElements = input(0);
  pageSize = input(20);
  pageIndex = input(0);
  pageSizeOptions = input([10, 20, 50, 100]);
  showActions = input(true);
  ariaLabel = input('Tabela podataka');

  pageChange = output<PageEvent>();
  sortChange = output<Sort>();
  editClick = output<T>();
  deleteClick = output<T>();

  displayedColumns = computed(() => {
    const cols = this.columns().map((c) => c.key);
    return this.showActions() ? [...cols, 'actions'] : cols;
  });
}
