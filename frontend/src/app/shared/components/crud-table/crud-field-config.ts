export interface CrudPdfHeader {
  kompanijaNaziv?: string;
  kompanijaAdresa?: string;
  kompanijaGrad?: string;
  poslovnicaNaziv?: string;
  poslovnicaAdresa?: string;
  poslovnicaGrad?: string;
}

export type CrudFieldType = 'text' | 'number' | 'email' | 'date' | 'time' | 'boolean' | 'select' | 'multiselect' | 'password';

export interface CrudFieldOption {
  label: string;
  value: any;
}

export interface CrudActionsConfig {
  add?: boolean;
  edit?: boolean;
  delete?: boolean;
  export?: boolean;
}

export interface CrudFieldConfig {
  key: string;
  label: string;
  type: CrudFieldType;
  visible?: boolean;
  required?: boolean;
  readOnly?: boolean;
  defaultValue?: any;
  placeholder?: string;
  options?: CrudFieldOption[];
  addOptions?: CrudFieldOption[]; // used instead of options when modalMode === 'add'
  hideEmptyOption?: boolean; // suppress the automatic "-- Nije odabrano --" null option
  validationMessage?: string;
  min?: number | string;
  max?: number | string;
  pattern?: string;
  requiredMessage?: string;
  minMessage?: string;
  maxMessage?: string;
  patternMessage?: string;
  generalErrorMessage?: string;
  requiredOnAdd?: boolean;
  readOnlyOnEdit?: boolean;
  dateFilterMode?: 'eq' | 'gte' | 'lte';
  decimals?: number;
  cellClass?: (value: any, row: any) => string;
}
