import { Component, inject } from '@angular/core';
import { Board, BoardService } from '../../core/boards/board.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-boards',
  imports: [FormsModule],
  templateUrl: './boards.html',
  styleUrl: './boards.scss',
})
export class Boards {

  private readonly boardService = inject(BoardService);

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
}
