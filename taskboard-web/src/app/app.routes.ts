import { Routes } from '@angular/router';
import { Login } from './features/login/login';
import { Boards } from './features/boards/boards';
import { authGuard } from './core/auth/auth.guard';
import { AppShell } from './layout/app-shell/app-shell';
import { BoardDetailComponent } from './features/board-detail/board-detail.component';

export const routes: Routes = [
  { path: 'login', component: Login },
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      { path: 'boards/:boardId', component: BoardDetailComponent },
      { path: 'boards', component: Boards },
      { path: '', pathMatch: 'full', redirectTo: 'boards' },
    ],
  },
];
