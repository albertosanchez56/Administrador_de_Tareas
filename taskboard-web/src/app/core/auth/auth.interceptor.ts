import { inject } from "@angular/core";
import { AuthService } from "./auth.service";
import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { catchError, switchMap, throwError } from 'rxjs';
import { Router } from "@angular/router";

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const token = auth.getToken();
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const isAuthUrl =
        req.url.includes('/api/auth/login') ||
        req.url.includes('/api/auth/register') ||
        req.url.includes('/api/auth/refresh');

      if (error.status !== 401 || isAuthUrl || !auth.getRefreshToken()) {
        if (error.status === 401 && !isAuthUrl) {
          auth.logout();
          router.navigateByUrl('/login');
        }
        return throwError(() => error);
      }

      // Access caducado → refrescar y reintentar
      return auth.refresh().pipe(
        switchMap(() => {
          const newToken = auth.getToken();
          const retry = req.clone({
            setHeaders: { Authorization: `Bearer ${newToken}` },
          });
          return next(retry);
        }),
        catchError((refreshErr) => {
          auth.logout();
          router.navigateByUrl('/login');
          return throwError(() => refreshErr);
        }),
      );
    }),
  );
};