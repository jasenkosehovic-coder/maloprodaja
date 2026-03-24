export type CrudFieldType = 'text' | 'number' | 'email' | 'date' | 'time' | 'boolean' | 'select' | 'multiselect';

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
  validationMessage?: string;
  min?: number | string;
  max?: number | string;
  pattern?: string;
  requiredMessage?: string;
  minMessage?: string;
  maxMessage?: string;
  patternMessage?: string;
  generalErrorMessage?: string;
}
