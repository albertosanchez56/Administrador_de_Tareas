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

  editingBoard: Board | null = null;
  boardEditTitle = '';
  boardEditDescription = '';
  savingBoard = false;
  boardEditError = false;

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

  openBoardEdit(board: Board, event?: Event) {
    event?.stopPropagation();
    this.editingBoard = board;
    this.boardEditTitle = board.title;
    this.boardEditDescription = board.description || '';
    this.boardEditError = false;
  }

  cancelBoardEdit() {
    this.editingBoard = null;
    this.boardEditTitle = '';
    this.boardEditDescription = '';
    this.savingBoard = false;
    this.boardEditError = false;
  }

  saveBoardEdit() {
    if (!this.editingBoard || !this.boardEditTitle.trim() || this.savingBoard) {
      return;
    }

    this.savingBoard = true;
    this.boardEditError = false;

    this.boardService
      .updateBoard(
        this.editingBoard.id,
        this.boardEditTitle.trim(),
        this.boardEditDescription.trim() || undefined,
      )
      .subscribe({
        next: () => {
          this.cancelBoardEdit();
          this.loadBoards();
        },
        error: () => {
          this.boardEditError = true;
          this.savingBoard = false;
        },
      });
  }

  deleteBoard() {
    if (!this.editingBoard) {
      return;
    }

    const ok = confirm('¿Eliminar este tablero? No se puede deshacer.');
    if (!ok) {
      return;
    }

    this.boardService.deleteBoard(this.editingBoard.id).subscribe({
      next: () => {
        this.cancelBoardEdit();
        this.loadBoards();
      },
      error: () => {
        this.boardEditError = true;
      },
    });
  }
}
