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

export interface BoardMember {
  boardId: number;
  userId: number;
  username: string;
  role: string;
  createdAt: string;
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

  getBoard(boardId: string) {
    return this.http.get<Board>(`/api/boards/${boardId}`);
  }

  getMembers(boardId: number | string) {
    return this.http.get<BoardMember[]>(`/api/boards/${boardId}/members`);
  } 

  inviteMember(boardId: number | string, email: string) {
    return this.http.post<BoardMember>(`/api/boards/${boardId}/members`, { email });
  }

  removeMember(boardId: number | string, userId: number) {
    return this.http.delete<void>(`/api/boards/${boardId}/members/${userId}`);
  }
}
