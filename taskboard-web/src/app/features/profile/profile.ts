import { Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, UserProfile } from '../../core/auth/auth.service';
import { PageHeader } from '../../shared/page-header/page-header';
import { getApiErrorMessage } from '../../core/http/api-error';

@Component({
  selector: 'app-profile',
  imports: [FormsModule, PageHeader, DatePipe, RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  profile: UserProfile | null = null;
  loading = true;
  errorMessage = '';

  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  saving = false;
  passwordError = '';
  passwordSuccess = '';

  constructor() {
    this.loadProfile();
  }

  loadProfile() {
    this.loading = true;
    this.errorMessage = '';
    this.auth.getMe().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = getApiErrorMessage(
          err,
          'No se pudo cargar el perfil.',
        );
      },
    });
  }

  onChangePassword() {
    this.passwordError = '';
    this.passwordSuccess = '';

    if (this.newPassword.length < 8) {
      this.passwordError = 'La contraseña nueva debe tener al menos 8 caracteres.';
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.passwordError = 'Las contraseñas nuevas no coinciden.';
      return;
    }
    if (!this.currentPassword) {
      this.passwordError = 'Introduce tu contraseña actual.';
      return;
    }

    this.saving = true;
    this.auth.changePassword(this.currentPassword, this.newPassword).subscribe({
      next: () => {
        this.saving = false;
        this.currentPassword = '';
        this.newPassword = '';
        this.confirmPassword = '';
        this.passwordSuccess = 'Contraseña actualizada correctamente.';
      },
      error: (err) => {
        this.saving = false;
        this.passwordError = getApiErrorMessage(
          err,
          'No se pudo cambiar la contraseña. Revisa la actual.',
        );
      },
    });
  }

  onLogout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
