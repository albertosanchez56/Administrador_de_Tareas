import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export type TaskStatus = 'TODO' | 'DOING' | 'DONE';

export interface Task {
  id: number;
  title: string;
  description: string | null;
  status: TaskStatus;
  position: number;
  boardId: number;
  createdBy: string;
  assignedTo: string | null;
  createdAt: string;
  updatedAt: string;
  dueAt: string | null;
}

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);

  getTasksByBoard(boardId: number | string) {
    return this.http.get<Task[]>(`/api/boards/${boardId}/tasks`);
  }

  createTask(boardId: number | string, title: string, description?: string) {
    return this.http.post<Task>(`/api/boards/${boardId}/tasks`, { title, description });
  }

  updateTask(
    boardId: number | string,
    taskId: number,
    body: {
      title?: string;
      description?: string | null;
      status?: TaskStatus;
      position?: number;
      assignedToUserId?: number;
      dueAt?: string;
    },
  ) {
    return this.http.patch<Task>(`/api/boards/${boardId}/tasks/${taskId}`, body);
  }
}
