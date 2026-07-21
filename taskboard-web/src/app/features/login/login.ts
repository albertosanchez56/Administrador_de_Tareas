import { Component, inject} from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {

  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  username: string = '';
  password: string = '';
  error: boolean = false;
  loading: boolean = false;

  onSubmit() {
    this.error = false;
    this.loading = true;
    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/boards']);
      },
      error: () => {
        this.loading = false;
        this.error = true;
      }
    });
  }
}
