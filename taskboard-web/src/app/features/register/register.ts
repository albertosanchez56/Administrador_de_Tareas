import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { getApiErrorMessage } from '../../core/http/api-error';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  username = '';
  email = '';
  password = '';
  confirmPassword = '';

  errorMessage: string = '';
  loading = false;

  onSubmit() {
    this.errorMessage = '';

    if (!this.username.trim() || !this.email.trim()) {
      this.errorMessage = 'Completa todos los campos.';
      return;
    }
    if (this.password.length < 8) {
      this.errorMessage = 'La contraseña debe tener al menos 8 caracteres.';
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Las contraseñas no coinciden.';
      return;
    }

    this.loading = true;
    this.auth.register(this.username.trim(), this.email.trim(), this.password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = getApiErrorMessage(
          err,
          'No hemos podido crear la cuenta. Inténtalo de nuevo.'
        );
      }
    });
  }
}
