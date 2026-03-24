import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  inject,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CrudActionsConfig, CrudFieldConfig, CrudFieldOption } from './crud-field-config';

// jsPDF imports
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-crud-table',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTooltipModule,
  ],
  templateUrl: './crud-table.component.html',
  styleUrl: './crud-table.component.scss',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class CrudTableComponent implements OnChanges {
  @Input() headers: string[] = [];
  @Input() data: any[] = [];
  @Input() fields: CrudFieldConfig[] = [];
  @Input() filterableColumns: string[] = [];
  @Input() actions: CrudActionsConfig = { add: true, edit: true, delete: true, export: true };
  @Input() exportName: string = 'Table Export';
  @Input() externalAdd = false;
  @Input() externalEdit = false;
  @Input() externalSave = false;
  @Input() externalDelete = false;
  @Input() rowStyleClass: ((row: any) => any) | null = null;
  @Input() saving = false;
  @Input() idField: string = 'id';

  @Output() selectionChange = new EventEmitter<any[]>();
  @Output() add = new EventEmitter<any>();
  @Output() addClick = new EventEmitter<void>();
  @Output() editClick = new EventEmitter<any>();
  @Output() edit = new EventEmitter<any>();
  @Output() delete = new EventEmitter<any>();
  @Output() deleteMany = new EventEmitter<any[]>();

  @ViewChild('editForm') editForm?: NgForm;
  @ViewChild('firstField') firstField?: ElementRef;

  private readonly cdr = inject(ChangeDetectorRef);

  // Pagination
  currentPage = 1;
  pageSize = 10;
  pageSizeOptions = [5, 10, 20, 50, 100];

  // Sorting
  sortedColumn: string | null = null;
  sortDirection: 'asc' | 'desc' | null = null;

  // Filters
  filters: Record<string, any> = {};
  dateFilters: Record<string, { from: Date | null; to: Date | null }> = {};

  // Selection
  selectedIds = new Set<any>();

  // Modal state
  modalVisible = false;
  modalMode: 'add' | 'edit' = 'add';
  editingRow: any = {};
  fieldErrors: Record<string, string | null> = {};

  // Delete modal
  deleteModalVisible = false;
  deleteTarget: any = null;
  deleteManyMode = false;

  // Internal data (for non-external operations)
  internalData: any[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data']) {
      this.internalData = [...(this.data || [])];
      this.currentPage = 1;
    }
  }

  // ─── Computed Data ───────────────────────────────────────────────────────────

  get filteredData(): any[] {
    return this.internalData.filter(row => {
      for (const key of this.effectiveFilterableColumns) {
        const field = this.fields.find(f => f.key === key);
        if (!field) continue;

        const filterVal = this.filters[key];
        const dateFilter = this.dateFilters[key];

        if (field.type === 'date') {
          const rawVal = row[key];
          if (!rawVal) continue;
          const cellDate = this.parseCellDate(rawVal);
          if (!cellDate) continue;
          if (dateFilter?.from) {
            const from = new Date(dateFilter.from);
            from.setHours(0, 0, 0, 0);
            if (cellDate < from) return false;
          }
          if (dateFilter?.to) {
            const to = new Date(dateFilter.to);
            to.setHours(23, 59, 59, 999);
            if (cellDate > to) return false;
          }
          continue;
        }

        if (filterVal === null || filterVal === undefined || filterVal === '') continue;

        const cellVal = row[key];

        if (field.type === 'boolean') {
          if (filterVal === 'true' && !cellVal) return false;
          if (filterVal === 'false' && cellVal) return false;
          continue;
        }

        if (field.type === 'select') {
          if (String(cellVal) !== String(filterVal)) return false;
          continue;
        }

        if (field.type === 'multiselect') {
          if (!Array.isArray(filterVal) || filterVal.length === 0) continue;
          const cellArr = Array.isArray(cellVal) ? cellVal : [cellVal];
          const hasMatch = filterVal.some((fv: any) =>
            cellArr.some((cv: any) => String(cv) === String(fv))
          );
          if (!hasMatch) return false;
          continue;
        }

        if (field.type === 'number') {
          if (String(cellVal) !== String(filterVal)) return false;
          continue;
        }

        if (field.type === 'time') {
          const norm = this.normalizeTime(String(cellVal ?? ''));
          const normFilter = this.normalizeTime(String(filterVal));
          if (!norm.includes(normFilter)) return false;
          continue;
        }

        // text / email
        const str = String(cellVal ?? '').toLowerCase();
        if (!str.includes(String(filterVal).toLowerCase())) return false;
      }
      return true;
    });
  }

  get sortedData(): any[] {
    if (!this.sortedColumn || !this.sortDirection) return this.filteredData;
    const col = this.sortedColumn;
    const dir = this.sortDirection;
    return [...this.filteredData].sort((a, b) => {
      let va = a[col];
      let vb = b[col];
      if (va == null) va = '';
      if (vb == null) vb = '';
      if (typeof va === 'number' && typeof vb === 'number') {
        return dir === 'asc' ? va - vb : vb - va;
      }
      return dir === 'asc'
        ? String(va).localeCompare(String(vb))
        : String(vb).localeCompare(String(va));
    });
  }

  get pagedData(): any[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.sortedData.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.sortedData.length / this.pageSize));
  }

  get effectiveFilterableColumns(): string[] {
    return this.filterableColumns.length > 0 ? this.filterableColumns : this.headers;
  }

  // ─── Sorting ─────────────────────────────────────────────────────────────────

  onHeaderClick(key: string): void {
    if (this.sortedColumn === key) {
      if (this.sortDirection === 'asc') {
        this.sortDirection = 'desc';
      } else if (this.sortDirection === 'desc') {
        this.sortedColumn = null;
        this.sortDirection = null;
      } else {
        this.sortDirection = 'asc';
      }
    } else {
      this.sortedColumn = key;
      this.sortDirection = 'asc';
    }
    this.currentPage = 1;
  }

  // ─── Selection ───────────────────────────────────────────────────────────────

  get allSelected(): boolean {
    return this.pagedData.length > 0 &&
      this.pagedData.every(row => this.selectedIds.has(row[this.idField]));
  }

  get someSelected(): boolean {
    return this.pagedData.some(row => this.selectedIds.has(row[this.idField])) && !this.allSelected;
  }

  toggleAll(checked: boolean): void {
    if (checked) {
      this.pagedData.forEach(row => this.selectedIds.add(row[this.idField]));
    } else {
      this.pagedData.forEach(row => this.selectedIds.delete(row[this.idField]));
    }
    this.emitSelection();
  }

  toggleRow(row: any): void {
    const id = row[this.idField];
    if (this.selectedIds.has(id)) {
      this.selectedIds.delete(id);
    } else {
      this.selectedIds.add(id);
    }
    this.emitSelection();
  }

  isSelected(row: any): boolean {
    return this.selectedIds.has(row[this.idField]);
  }

  onRowClick(event: MouseEvent, row: any): void {
    const target = event.target as HTMLElement;
    if (
      target.closest('button') ||
      target.closest('mat-checkbox') ||
      target.closest('input') ||
      target.closest('mat-icon')
    ) {
      return;
    }
    this.toggleRow(row);
  }

  private emitSelection(): void {
    const selected = this.internalData.filter(row => this.selectedIds.has(row[this.idField]));
    this.selectionChange.emit(selected);
  }

  get selectedCount(): number {
    return this.selectedIds.size;
  }

  // ─── Add / Edit Modal ────────────────────────────────────────────────────────

  get visibleFields(): CrudFieldConfig[] {
    return this.fields.filter(f => f.visible !== false);
  }

  onAddClick(): void {
    if (this.externalAdd) {
      this.addClick.emit();
      return;
    }
    this.modalMode = 'add';
    this.editingRow = this.buildDefaultRow();
    this.fieldErrors = {};
    this.modalVisible = true;
    setTimeout(() => this.focusFirstField(), 100);
  }

  onEditClick(row: any): void {
    if (this.externalEdit) {
      this.editClick.emit(row);
      return;
    }
    this.modalMode = 'edit';
    this.editingRow = this.normalizeRowForForm({ ...row });
    this.fieldErrors = {};
    this.modalVisible = true;
    setTimeout(() => this.focusFirstField(), 100);
  }

  private buildDefaultRow(): any {
    const row: any = {};
    for (const field of this.fields) {
      if (field.defaultValue !== undefined) {
        row[field.key] = field.defaultValue;
      } else if (field.type === 'boolean') {
        row[field.key] = false;
      } else if (field.type === 'multiselect') {
        row[field.key] = [];
      } else {
        row[field.key] = null;
      }
    }
    return row;
  }

  private normalizeRowForForm(row: any): any {
    for (const field of this.fields) {
      if (field.type === 'date' && row[field.key]) {
        row[field.key] = this.toInputDate(row[field.key]);
      } else if (field.type === 'time' && row[field.key]) {
        row[field.key] = this.toInputTime(row[field.key]);
      } else if (field.type === 'multiselect' && !Array.isArray(row[field.key])) {
        row[field.key] = row[field.key] ? [row[field.key]] : [];
      }
    }
    return row;
  }

  closeModal(): void {
    this.modalVisible = false;
    this.editingRow = {};
    this.fieldErrors = {};
  }

  onSaveEdit(): void {
    if (!this.isFormValid()) return;

    const row = this.prepareRowForSave({ ...this.editingRow });

    if (this.modalMode === 'add') {
      if (!this.externalSave) {
        this.internalData = [...this.internalData, row];
      }
      this.add.emit(row);
    } else {
      if (!this.externalSave) {
        this.internalData = this.internalData.map(r =>
          r[this.idField] === row[this.idField] ? row : r
        );
      }
      this.edit.emit(row);
    }

    this.closeModal();
  }

  private prepareRowForSave(row: any): any {
    for (const field of this.fields) {
      if (field.type === 'date' && row[field.key]) {
        row[field.key] = this.fromInputDate(row[field.key]);
      } else if (field.type === 'time' && row[field.key]) {
        row[field.key] = this.fromInputTime(row[field.key]);
      }
    }
    return row;
  }

  private isFormValid(): boolean {
    let valid = true;
    this.fieldErrors = {};
    for (const field of this.visibleFields) {
      if (field.readOnly) continue;
      const val = this.editingRow[field.key];
      const error = this.validateField(field, val);
      if (error) {
        this.fieldErrors[field.key] = error;
        valid = false;
      }
    }
    return valid;
  }

  private validateField(field: CrudFieldConfig, value: any): string | null {
    const isEmpty = value === null || value === undefined || value === '';

    if (field.required && isEmpty) {
      return field.requiredMessage ?? `${field.label} is required.`;
    }

    if (isEmpty) return null;

    if (field.type === 'number') {
      const num = Number(value);
      if (field.min !== undefined && num < Number(field.min)) {
        return field.minMessage ?? `${field.label} must be at least ${field.min}.`;
      }
      if (field.max !== undefined && num > Number(field.max)) {
        return field.maxMessage ?? `${field.label} must be at most ${field.max}.`;
      }
    }

    if (field.type === 'date') {
      if (field.min && value < String(field.min)) {
        return field.minMessage ?? `${field.label} must be on or after ${field.min}.`;
      }
      if (field.max && value > String(field.max)) {
        return field.maxMessage ?? `${field.label} must be on or before ${field.max}.`;
      }
    }

    if (field.type === 'time') {
      const valMins = this.parseTimeToMinutes(value);
      if (field.min !== undefined) {
        const minMins = this.parseTimeToMinutes(String(field.min));
        if (valMins < minMins) {
          return field.minMessage ?? `${field.label} must be at or after ${field.min}.`;
        }
      }
      if (field.max !== undefined) {
        const maxMins = this.parseTimeToMinutes(String(field.max));
        if (valMins > maxMins) {
          return field.maxMessage ?? `${field.label} must be at or before ${field.max}.`;
        }
      }
    }

    if (field.pattern && !new RegExp(field.pattern).test(String(value))) {
      return field.patternMessage ?? `${field.label} has invalid format.`;
    }

    return null;
  }

  onFieldChange(field: CrudFieldConfig): void {
    const val = this.editingRow[field.key];
    this.fieldErrors[field.key] = this.validateField(field, val);
  }

  private focusFirstField(): void {
    const modal = document.querySelector('.crud-modal-body input, .crud-modal-body mat-select');
    if (modal) (modal as HTMLElement).focus();
  }

  // ─── Delete ───────────────────────────────────────────────────────────────────

  onDeleteClick(row: any): void {
    this.deleteTarget = row;
    this.deleteManyMode = false;
    this.deleteModalVisible = true;
  }

  onDeleteManyClick(): void {
    if (this.selectedIds.size === 0) return;
    this.deleteManyMode = true;
    this.deleteModalVisible = true;
  }

  confirmDelete(): void {
    if (this.deleteManyMode) {
      const toDelete = this.internalData.filter(row => this.selectedIds.has(row[this.idField]));
      if (!this.externalDelete) {
        this.internalData = this.internalData.filter(row => !this.selectedIds.has(row[this.idField]));
      }
      this.selectedIds.clear();
      this.deleteMany.emit(toDelete);
    } else if (this.deleteTarget) {
      if (!this.externalDelete) {
        this.internalData = this.internalData.filter(
          row => row[this.idField] !== this.deleteTarget[this.idField]
        );
        this.selectedIds.delete(this.deleteTarget[this.idField]);
      }
      this.delete.emit(this.deleteTarget);
    }
    this.closeDeleteModal();
    this.emitSelection();
  }

  closeDeleteModal(): void {
    this.deleteModalVisible = false;
    this.deleteTarget = null;
  }

  // ─── Pagination ───────────────────────────────────────────────────────────────

  get paginationPages(): number[] {
    const total = this.totalPages;
    const current = this.currentPage;
    const maxVisible = 10;

    if (total <= maxVisible) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }

    const pages: number[] = [1];
    let start = Math.max(2, current - 4);
    let end = Math.min(total - 1, current + 4);

    if (current <= 5) {
      start = 2;
      end = Math.min(total - 1, maxVisible - 2);
    } else if (current >= total - 4) {
      start = Math.max(2, total - maxVisible + 2);
      end = total - 1;
    }

    if (start > 2) pages.push(-1); // left dots marker
    for (let i = start; i <= end; i++) pages.push(i);
    if (end < total - 1) pages.push(-2); // right dots marker
    if (total > 1) pages.push(total);

    return pages;
  }

  get hasLeftDots(): boolean {
    const pages = this.paginationPages;
    return pages.includes(-1);
  }

  get hasRightDots(): boolean {
    const pages = this.paginationPages;
    return pages.includes(-2);
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
  }

  prevPage(): void {
    if (this.currentPage > 1) this.currentPage--;
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) this.currentPage++;
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
  }

  // ─── Date Filters ─────────────────────────────────────────────────────────────

  getDateFilter(key: string): { from: Date | null; to: Date | null } {
    if (!this.dateFilters[key]) {
      this.dateFilters[key] = { from: null, to: null };
    }
    return this.dateFilters[key];
  }

  setDateFilterFrom(key: string, date: Date | null): void {
    this.getDateFilter(key).from = date;
    this.currentPage = 1;
  }

  setDateFilterTo(key: string, date: Date | null): void {
    this.getDateFilter(key).to = date;
    this.currentPage = 1;
  }

  hasActiveDateFilter(key: string): boolean {
    const df = this.dateFilters[key];
    return !!(df?.from || df?.to);
  }

  clearDateFilter(key: string): void {
    this.dateFilters[key] = { from: null, to: null };
    this.currentPage = 1;
  }

  onFilterChange(): void {
    this.currentPage = 1;
  }

  // ─── Field Helpers ────────────────────────────────────────────────────────────

  getField(key: string): CrudFieldConfig | undefined {
    return this.fields.find(f => f.key === key);
  }

  getFieldType(key: string): string {
    return this.getField(key)?.type ?? 'text';
  }

  isFilterable(key: string): boolean {
    return this.effectiveFilterableColumns.includes(key);
  }

  getFieldOptions(key: string): CrudFieldOption[] {
    return this.getField(key)?.options ?? [];
  }

  getColClass(key: string): string {
    const type = this.getFieldType(key);
    switch (type) {
      case 'number': return 'col-number';
      case 'date': return 'col-date';
      case 'time': return 'col-time';
      case 'boolean': return 'col-boolean';
      case 'select': return 'col-select';
      case 'multiselect': return 'col-multiselect';
      default: return 'col-text';
    }
  }

  // ─── Cell Display ─────────────────────────────────────────────────────────────

  formatCellValue(row: any, key: string): string {
    const val = row[key];
    const field = this.getField(key);
    if (val === null || val === undefined) return '';

    if (field?.type === 'boolean') return val ? 'Yes' : 'No';

    if (field?.type === 'date') {
      const d = this.parseCellDate(val);
      if (!d) return String(val);
      const dd = String(d.getDate()).padStart(2, '0');
      const mm = String(d.getMonth() + 1).padStart(2, '0');
      const yyyy = d.getFullYear();
      return `${dd}-${mm}-${yyyy}`;
    }

    if (field?.type === 'time') {
      return this.normalizeTimeDisplay(String(val));
    }

    if (field?.type === 'select') {
      const opt = field.options?.find(o => String(o.value) === String(val));
      return opt ? opt.label : String(val);
    }

    if (field?.type === 'multiselect') {
      if (!Array.isArray(val)) return String(val);
      return val
        .map(v => {
          const opt = field.options?.find(o => String(o.value) === String(v));
          return opt ? opt.label : String(v);
        })
        .join(', ');
    }

    return String(val);
  }

  getCellTooltip(row: any, key: string): string {
    return this.formatCellValue(row, key);
  }

  // ─── Date Utilities ───────────────────────────────────────────────────────────

  private parseCellDate(val: any): Date | null {
    if (!val) return null;
    if (val instanceof Date) return val;
    const d = new Date(val);
    return isNaN(d.getTime()) ? null : d;
  }

  toInputDate(val: any): string {
    const d = this.parseCellDate(val);
    if (!d) return '';
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  fromInputDate(val: string): string {
    return val;
  }

  // ─── Time Utilities ───────────────────────────────────────────────────────────

  private normalizeTime(time: string): string {
    return time.replace(/[^0-9:]/g, '');
  }

  private normalizeTimeDisplay(time: string): string {
    if (!time) return '';
    const parts = time.split(':');
    const hh = (parts[0] ?? '00').padStart(2, '0');
    const mm = (parts[1] ?? '00').padStart(2, '0');
    return `${hh}:${mm}`;
  }

  toInputTime(val: any): string {
    return this.normalizeTimeDisplay(String(val ?? ''));
  }

  fromInputTime(val: string): string {
    return val;
  }

  parseTimeToMinutes(time: string): number {
    if (!time) return 0;
    const parts = time.split(':');
    return parseInt(parts[0] ?? '0', 10) * 60 + parseInt(parts[1] ?? '0', 10);
  }

  // ─── Export ───────────────────────────────────────────────────────────────────

  exportFilteredToCsv(): void {
    const visibleHeaders = this.headers;
    const labels = visibleHeaders.map(h => this.getField(h)?.label ?? h);

    const rows = this.sortedData.map(row =>
      visibleHeaders.map(h => {
        const val = this.formatCellValue(row, h);
        return `"${String(val).replace(/"/g, '""')}"`;
      }).join(',')
    );

    const csv = [labels.join(','), ...rows].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${this.exportName}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  exportPdf(): void {
    const doc = new jsPDF({ orientation: 'landscape', format: 'a4' });
    const visibleHeaders = this.headers;
    const labels = visibleHeaders.map(h => this.getField(h)?.label ?? h);

    const bodyData = this.sortedData.map(row =>
      visibleHeaders.map(h => this.formatCellValue(row, h))
    );

    const now = new Date();
    const dateStr = `${now.getDate().toString().padStart(2, '0')}-${(now.getMonth() + 1).toString().padStart(2, '0')}-${now.getFullYear()} ${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;

    doc.setFontSize(14);
    doc.text(this.exportName, 14, 15);
    doc.setFontSize(9);
    doc.text(`Exported: ${dateStr}`, 14, 22);

    autoTable(doc, {
      head: [labels],
      body: bodyData,
      startY: 28,
      styles: { fontSize: 8, cellPadding: 2 },
      headStyles: { fillColor: [63, 81, 181] },
    });

    doc.save(`${this.exportName}.pdf`);
  }

  // ─── Modal form helpers ───────────────────────────────────────────────────────

  getModalTitle(): string {
    return this.modalMode === 'add' ? 'Add item' : 'Edit item';
  }

  getInputType(field: CrudFieldConfig): string {
    switch (field.type) {
      case 'number': return 'number';
      case 'email': return 'email';
      case 'date': return 'date';
      case 'time': return 'time';
      default: return 'text';
    }
  }

  isSimpleInput(field: CrudFieldConfig): boolean {
    return ['text', 'number', 'email', 'date', 'time'].includes(field.type);
  }

  // ─── Pagination display helpers ───────────────────────────────────────────────

  isPaginationDots(page: number): boolean {
    return page < 0;
  }

  get startRecord(): number {
    if (this.sortedData.length === 0) return 0;
    return (this.currentPage - 1) * this.pageSize + 1;
  }

  get endRecord(): number {
    return Math.min(this.currentPage * this.pageSize, this.sortedData.length);
  }
}
