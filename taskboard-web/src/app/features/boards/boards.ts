import { Component, inject } from '@angular/core';
import { Board, BoardService } from '../../core/boards/board.service';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-boards',
  imports: [FormsModule],
  templateUrl: './boards.html',
  styleUrl: './boards.scss',
})
export class Boards {

  private readonly boardService = inject(BoardService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  boards: Board[] = [];
  loading = false;
  error = false;
  title = '';
  description = '';

  constructor() {
    this.loadBoards();
  }
  loadBoards() {
    this.boardService.getMyBoards().subscribe({
      next: (boards) => {
        this.boards = boards;
      },
      error: () => {
        this.error = true;
      },
    });
  }

  onCreate() {
    if (!this.title.trim()) {
      return;
    }

    this.boardService.createBoard(this.title.trim(), this.description.trim() || undefined)
      .subscribe({
        next: () => {
          this.title = '';
          this.description = '';
          this.loadBoards(); // recarga la lista
        },
        error: () => {
          this.error = true;
        },
      });
  }

  onLogout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
