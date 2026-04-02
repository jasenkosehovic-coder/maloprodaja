import { bootstrapApplication } from '@angular/platform-browser';
import { registerLocaleData } from '@angular/common';
import localeBs from '@angular/common/locales/bs';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

registerLocaleData(localeBs, 'bs-BA');

bootstrapApplication(AppComponent, appConfig).catch((err: unknown) =>
  console.error(err)
);
