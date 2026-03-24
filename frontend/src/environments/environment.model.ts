export interface Environment {
  production: boolean;
  apiUrl: string;
  wsUrl: string;
  appName?: string;
  version?: string;
  inactivityTimeoutMs: number;
}
