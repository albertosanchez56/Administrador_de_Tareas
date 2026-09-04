import { Component, inject} from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { getApiErrorMessage } from '../../core/http/api-error';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {

  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  username: string = '';
  password: string = '';
  errorMessage: string = '';
  loading: boolean = false;

  onSubmit() {
    this.errorMessage = '';
    this.loading = true;
    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/boards']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = getApiErrorMessage(
          err,
          'No hemos podido entrar. Revisa usuario y contraseña.'
        );
      },
    });
  }
}
