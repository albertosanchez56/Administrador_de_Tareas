import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';

export interface LoginResponse {
    accessToken: string;
    userId: number;
    username: string;
    role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly http = inject(HttpClient);
    private readonly tokenKey = 'accessToken';


    login(username: string, password: string) {
        return this.http.post<LoginResponse>('/api/auth/login', { username, password }).pipe(
            tap(response => {
                localStorage.setItem(this.tokenKey, response.accessToken);
                localStorage.setItem('userId', String(response.userId));
            })
        );
    }

    getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
    }

    isLoggedIn(): boolean {
        return this.isTokenValid();
    }

    logout(): void {
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem('userId');
    }

    getUserId(): number | null {
        const raw = localStorage.getItem('userId');
        return raw ? Number(raw) : null;
    }

    register(username: string, email: string, password: string) {
        return this.http.post('/api/auth/register', { username, email, password });
    }

    // Compruebo si el JWT sigue vivo. No valido la firma (eso lo hace el back);
    // aquí solo miro si existe y si el claim exp aún no ha pasado.
    isTokenValid(): boolean {
        const token = this.getToken();
        if (!token) {
            return false;
        }

        try {
            // Un JWT son 3 partes separadas por '.': header.payload.firma
            // Me quedo con la del medio, que es el JSON con sub, exp, roles, etc.
            const payloadPart = token.split('.')[1];
            if (!payloadPart) {
                return false;
            }

            // El payload viene en base64url (- y _ en vez de + y /).
            // Lo paso a base64 normal y lo decodifico con atob.
            const json = atob(payloadPart.replace(/-/g, '+').replace(/_/g, '/'));
            const payload = JSON.parse(json) as { exp?: number };

            if (!payload.exp) {
                return false;
            }

            // exp viene en segundos y Date.now() en milisegundos.
            return Date.now() < payload.exp * 1000;
        } catch {
            // Token mal formado o no se puede parsear -> lo trato como inválido.
            return false;
        }
    }
}