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
        return !!this.getToken();
    }

    logout(): void {
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem('userId');
    }

    getUserId(): number | null {
        const raw = localStorage.getItem('userId');
        return raw ? Number(raw) : null;
    }
}