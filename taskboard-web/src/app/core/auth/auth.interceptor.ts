import { inject } from "@angular/core";
import { AuthService } from "./auth.service";
import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { catchError, throwError } from "rxjs";
import { Router } from "@angular/router";

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }
  return next(req).pipe(catchError((error: HttpErrorResponse) => {
    const isAuthUrl =
      req.url.includes('/api/auth/login') ||
      req.url.includes('/api/auth/register');
    if (!isAuthUrl) {
      const sessionDead =
        error.status === 401 ||
        (error.status === 403 && !authService.isTokenValid());
      if (sessionDead) {
        authService.logout();
        router.navigateByUrl('/login');
      }
    }
    return throwError(() => error);
  }));
}