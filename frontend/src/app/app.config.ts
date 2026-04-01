import {
  ApplicationConfig,
  provideZoneChangeDetection,
  LOCALE_ID,
} from '@angular/core';
import { provideRouter, withComponentInputBinding, withViewTransitions } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import {
  provideNativeDateAdapter,
  MAT_DATE_LOCALE,
  MAT_DATE_FORMATS,
  MatDateFormats,
} from '@angular/material/core';

import { APP_ROUTES } from './app.routes';
import { authInterceptor } from './core/auth/interceptors/auth.interceptor';
import { errorInterceptor } from './core/auth/interceptors/error.interceptor';

const BS_DATE_FORMATS: MatDateFormats = {
  parse: {
    dateInput: { day: 'numeric', month: 'numeric', year: 'numeric' },
  },
  display: {
    dateInput: { day: '2-digit', month: '2-digit', year: 'numeric' },
    monthYearLabel: { year: 'numeric', month: 'short' },
    dateA11yLabel: { year: 'numeric', month: 'long', day: 'numeric' },
    monthYearA11yLabel: { year: 'numeric', month: 'long' },
  },
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(
      APP_ROUTES,
      withComponentInputBinding(),
      withViewTransitions()
    ),
    provideHttpClient(
      withInterceptors([authInterceptor, errorInterceptor])
    ),
    provideAnimationsAsync(),
    provideNativeDateAdapter(),
    { provide: MAT_DATE_LOCALE, useValue: 'bs-BA' },
    { provide: MAT_DATE_FORMATS, useValue: BS_DATE_FORMATS },
    { provide: LOCALE_ID, useValue: 'bs-BA' },
  ],
};
