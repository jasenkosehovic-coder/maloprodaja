import { Environment } from './environment.model';

export const environment: Environment = {
  production: true,
  apiUrl: '/api',
  wsUrl: '/ws',
  appName: 'Maloprodaja',
  version: '1.0.0',
  inactivityTimeoutMs: 3600000,
};
