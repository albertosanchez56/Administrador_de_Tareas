import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export interface Board {
  id: number;
  title: string;
  description: string | null;
  ownerUserId: number;
  createdAt: string;
  updatedAt: string;
}


@Injectable({
  providedIn: 'root',
})
export class BoardService {

  private readonly http = inject(HttpClient);

  getMyBoards() {
    return this.http.get<Board[]>('/api/boards');
  }
  createBoard(title: string, description?: string) {
    return this.http.post<Board>('/api/boards', { title, description });
  }

}
