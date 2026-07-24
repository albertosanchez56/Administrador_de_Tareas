import { Routes } from '@angular/router';
import { Login } from './features/login/login';
import { Boards } from './features/boards/boards';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'boards', component: Boards, canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
];
