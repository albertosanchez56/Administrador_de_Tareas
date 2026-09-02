import { Component, inject } from '@angular/core';
import { Board, BoardService } from '../../core/boards/board.service';
import { FormsModule } from '@angular/forms';
import { PageHeader } from '../../shared/page-header/page-header';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-boards',
  imports: [FormsModule, PageHeader, DatePipe],
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

  creatingBoard = false;
  createTitle = '';
  createDescription = '';
  savingCreate = false;
  createError = false;

  editingBoard: Board | null = null;
  boardEditTitle = '';
  boardEditDescription = '';
  savingBoard = false;
  boardEditError = false;

  searchQuery = '';

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

  openBoard(boardId: number) {
    this.router.navigate(['/boards', boardId]);
  }

  isBoardOwner(board: Board): boolean {
    const me = this.auth.getUserId();
    return me != null && me === board.ownerUserId;
  }

  get ownedBoardsCount(): number {
    return this.boards.filter((b) => this.isBoardOwner(b)).length;
  }

  get memberBoardsCount(): number {
    return this.boards.length - this.ownedBoardsCount;
  }

  get filteredBoards(): Board[] {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) {
      return this.boards;
    }
    return this.boards.filter(
      (b) =>
        b.title.toLowerCase().includes(q) ||
        (b.description?.toLowerCase().includes(q) ?? false),
    );
  }

  openCreateBoard() {
    this.cancelBoardEdit();
    this.creatingBoard = true;
    this.createTitle = '';
    this.createDescription = '';
    this.createError = false;
  }

  cancelCreateBoard() {
    this.creatingBoard = false;
    this.createTitle = '';
    this.createDescription = '';
    this.savingCreate = false;
    this.createError = false;
  }

  saveCreateBoard() {
    if (!this.createTitle.trim() || this.savingCreate) {
      return;
    }

    this.savingCreate = true;
    this.createError = false;

    this.boardService
      .createBoard(this.createTitle.trim(), this.createDescription.trim() || undefined)
      .subscribe({
        next: () => {
          this.cancelCreateBoard();
          this.loadBoards();
        },
        error: () => {
          this.createError = true;
          this.savingCreate = false;
        },
      });
  }

  openBoardEdit(board: Board, event?: Event) {
    event?.stopPropagation();
    this.cancelCreateBoard();
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
