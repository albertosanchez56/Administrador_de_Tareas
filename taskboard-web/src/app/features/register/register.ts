import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

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

  error = false;
  loading = false;

  onSubmit() {
    this.error = false;

    if (
      !this.username.trim() ||
      !this.email.trim() ||
      this.password.length < 8 ||
      this.password !== this.confirmPassword
    ) {
      this.error = true;
      return;
    }

    this.loading = true;
    this.auth.register(this.username.trim(), this.email.trim(), this.password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/login']);
      },
      error: () => {
        this.loading = false;
        this.error = true;
      },
    });
  }
}
