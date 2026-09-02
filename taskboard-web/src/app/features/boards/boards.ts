import { Component, inject } from '@angular/core';
import { Board, BoardService } from '../../core/boards/board.service';
import { FormsModule } from '@angular/forms';
import { PageHeader } from '../../shared/page-header/page-header';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-boards',
  imports: [FormsModule, PageHeader],
  templateUrl: './boards.html',
  styleUrl: './boards.scss',
})
export class Boards {

  private readonly boardService = inject(BoardService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  
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
          this.loadBoards();
        },
        error: () => {
          this.error = true;
        },
      });
  }

  openBoard(boardId: number) {
    this.router.navigate(['/boards', boardId]);
  }

  isBoardOwner(board: Board): boolean {
    const me = this.auth.getUserId();
    return me != null && me === board.ownerUserId;
  }
}
