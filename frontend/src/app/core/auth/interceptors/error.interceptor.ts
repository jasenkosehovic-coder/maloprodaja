import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { NotificationService } from '../../services/notification.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notification = inject(NotificationService);

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 0) {
        notification.error('Nema konekcije sa serverom.');
      } else if (error.status === 403) {
        notification.error('Nemate dozvolu za ovu akciju.');
      } else if (error.status === 404) {
        notification.error('Traženi resurs nije pronađen.');
      } else if (error.status >= 500) {
        notification.error('Greška na serveru. Pokušajte ponovo.');
      }
      return throwError(() => error);
    })
  );
};
