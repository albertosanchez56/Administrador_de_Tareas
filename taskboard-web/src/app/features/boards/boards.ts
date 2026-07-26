import { Component, inject } from '@angular/core';
import { Board, BoardService } from '../../core/boards/board.service';

@Component({
  selector: 'app-boards',
  imports: [],
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
}
