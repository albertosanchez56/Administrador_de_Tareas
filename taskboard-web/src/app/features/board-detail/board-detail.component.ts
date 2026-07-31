import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  CdkDrag,
  CdkDragDrop,
  CdkDragPreview,
  CdkDropList,
  CdkDropListGroup,
  moveItemInArray,
  transferArrayItem,
} from '@angular/cdk/drag-drop';
import { PageHeader } from '../../shared/page-header/page-header';
import { Task, TaskService, TaskStatus } from '../../core/tasks/task.service';
import { BoardService } from '../../core/boards/board.service';

@Component({
  selector: 'app-board-detail',
  imports: [
    PageHeader,
    RouterLink,
    FormsModule,
    CdkDropList,
    CdkDrag,
    CdkDragPreview,
    CdkDropListGroup,
  ],
  templateUrl: './board-detail.component.html',
  styleUrl: './board-detail.component.scss',
})
export class BoardDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly tasksApi = inject(TaskService);
  private readonly boardApi = inject(BoardService);

  boardId = this.route.snapshot.paramMap.get('boardId') ?? '';
  boardTitle = 'Tablero';

  todo: Task[] = [];
  doing: Task[] = [];
  done: Task[] = [];

  newTitle = '';
  newDescription = '';
  loading = false;
  error = false;

  editingTask: Task | null = null;
  editTitle = '';
  editDescription = '';
  savingEdit = false;
  private dragging = false;

  constructor() {
    this.loadBoard();
    this.loadTasks();
  }

  loadTasks() {
    this.loading = true;
    this.error = false;
    this.tasksApi.getTasksByBoard(this.boardId).subscribe({
      next: (tasks) => {
        this.todo = tasks
          .filter((t) => t.status === 'TODO')
          .sort((a, b) => a.position - b.position);
        this.doing = tasks
          .filter((t) => t.status === 'DOING')
          .sort((a, b) => a.position - b.position);
        this.done = tasks
          .filter((t) => t.status === 'DONE')
          .sort((a, b) => a.position - b.position);
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      },
    });
  }

  loadBoard() {
    const id = Number(this.boardId);
    this.boardApi.getMyBoards().subscribe({
      next: (boards) => {
        const board = boards.find((b) => b.id === id);
        if (board) {
          this.boardTitle = board.title;
        }
      },
    });
  }

  onCreate() {
    if (!this.newTitle.trim()) {
      return;
    }

    this.tasksApi
      .createTask(this.boardId, this.newTitle.trim(), this.newDescription.trim() || undefined)
      .subscribe({
        next: () => {
          this.newTitle = '';
          this.newDescription = '';
          this.loadTasks();
        },
        error: () => {
          this.error = true;
        },
      });
  }

  drop(event: CdkDragDrop<Task[]>, targetStatus: TaskStatus) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
    }

    const task = event.container.data[event.currentIndex];
    task.status = targetStatus;

    this.tasksApi
      .updateTask(this.boardId, task.id, {
        status: targetStatus,
        position: event.currentIndex,
      })
      .subscribe({
        next: () => this.loadTasks(),
        error: () => {
          this.error = true;
          this.loadTasks();
        },
      });
  }

  onDragStarted() {
    this.dragging = true;
  }

  onDragEnded() {
    // El click del ratón llega después de soltar; ignoramos ese click.
    setTimeout(() => {
      this.dragging = false;
    }, 0);
  }

  openEdit(task: Task) {
    if (this.dragging) {
      return;
    }
    this.editingTask = task;
    this.editTitle = task.title;
    this.editDescription = task.description ?? '';
  }

  cancelEdit() {
    this.editingTask = null;
    this.editTitle = '';
    this.editDescription = '';
    this.savingEdit = false;
  }

  saveEdit() {
    if (!this.editingTask || !this.editTitle.trim() || this.savingEdit) {
      return;
    }

    this.savingEdit = true;
    this.tasksApi
      .updateTask(this.boardId, this.editingTask.id, {
        title: this.editTitle.trim(),
        description: this.editDescription.trim(),
      })
      .subscribe({
        next: () => {
          this.cancelEdit();
          this.loadTasks();
        },
        error: () => {
          this.error = true;
          this.savingEdit = false;
        },
      });
  }
}
