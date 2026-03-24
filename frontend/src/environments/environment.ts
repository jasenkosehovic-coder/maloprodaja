import { Environment } from './environment.model';

export const environment: Environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  wsUrl: 'http://localhost:8080/ws',
  appName: 'Maloprodaja',
  version: '1.0.0',
  inactivityTimeoutMs: 3600000,
};
