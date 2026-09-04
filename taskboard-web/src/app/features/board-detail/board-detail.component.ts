import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute,Router, RouterLink } from '@angular/router';
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
import { BoardMember, BoardService } from '../../core/boards/board.service';
import { DatePipe } from '@angular/common';
import { AuthService } from '../../core/auth/auth.service';
import { getApiErrorMessage } from '../../core/http/api-error';

@Component({
  selector: 'app-board-detail',
  imports: [
    PageHeader,
    RouterLink,
    FormsModule,
    DatePipe,
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
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

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
  editDueAt = '';
  savingEdit = false;
  private dragging = false;

  members: BoardMember[] = [];
  editAssignedToUserId: number | null = null;
  membersOpen = false;

  inviteEmail = '';
  inviting = false;
  inviteErrorMessage = '';
  selectedMemberId: number | null = null;
  removingMember = false;

  boardOwnerUserId: number | null = null;
  editingBoard = false;

  savingBoard = false;
  boardEditError = false;

  boardDescription = '';
  boardEditTitle = '';
  boardEditDescription = '';

  constructor() {
    this.loadBoard();
    this.loadTasks();
    this.loadMembers();
  }

  loadTasks(showLoading = true) {
    if (showLoading) {
      this.loading = true;
    }
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
    this.boardApi.getBoard(this.boardId).subscribe({
      next: (board) => {
        this.boardTitle = board.title;
        this.boardOwnerUserId = board.ownerUserId;
        this.boardDescription = board.description ?? '';
      },
      error: () => {
        this.error = true;
      },
    });
  }

  loadMembers() {
    this.boardApi.getMembers(this.boardId).subscribe({
      next: (members) => (this.members = members),
      error: () => (this.error = true),
    });
  }

  toggleMembers() {
    this.membersOpen = !this.membersOpen;
  }

  memberInitial(username: string): string {
    return username.trim().charAt(0).toUpperCase() || '?';
  }

  toggleMemberDetail(member: BoardMember) {
    this.selectedMemberId =
      this.selectedMemberId === member.userId ? null : member.userId;
  }

  canRemoveMember(member: BoardMember): boolean {
    return member.role !== 'OWNER';
  }

  onRemoveMember(member: BoardMember, event: Event) {
    event.stopPropagation();
    if (!this.canRemoveMember(member) || this.removingMember) {
      return;
    }

    const ok = confirm(
      `¿Quitar a ${member.username} de este tablero?\nPodrá volver a ser invitado más adelante.`,
    );
    if (!ok) {
      return;
    }

    this.removingMember = true;
    this.boardApi.removeMember(this.boardId, member.userId).subscribe({
      next: () => {
        if (this.selectedMemberId === member.userId) {
          this.selectedMemberId = null;
        }
        if (this.editAssignedToUserId === member.userId) {
          this.editAssignedToUserId = null;
        }
        this.removingMember = false;
        this.unassignLocalTasks(member.username);
        this.loadMembers();
        this.loadTasks(false);
      },
      error: () => {
        this.removingMember = false;
        this.error = true;
      },
    });
  }

  private unassignLocalTasks(username: string) {
    const clear = (tasks: Task[]) =>
      tasks.map((task) =>
        task.assignedTo === username ? { ...task, assignedTo: null } : task,
      );
    this.todo = clear(this.todo);
    this.doing = clear(this.doing);
    this.done = clear(this.done);
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
          this.loadTasks(false);
        },
        error: () => {
          this.error = true;
        },
      });
  }


  deleteTask(taskId: number) {
    const ok = confirm('¿Eliminar esta tarea? No se puede deshacer.');
    if (!ok) {
      return;
    }
    this.tasksApi.deleteTask(this.boardId, taskId).subscribe({
      next: () => {
        this.cancelEdit();
        this.loadTasks(false);
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
        next: () => this.loadTasks(false),
        error: () => {
          this.error = true;
          this.loadTasks(false);
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
    this.editDueAt = task.dueAt
      ? new Date(task.dueAt).toISOString().slice(0, 16)
      : '';
    this.editAssignedToUserId =
      this.members.find((m) => m.username === task.assignedTo)?.userId ?? null;
  }

  cancelEdit() {
    this.editingTask = null;
    this.editTitle = '';
    this.editDescription = '';
    this.savingEdit = false;
    this.editDueAt = '';
    this.editAssignedToUserId = null;
  }

  clearDueAt() {
    this.editDueAt = '';
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
        ...(this.editDueAt.trim()
          ? { dueAt: new Date(this.editDueAt).toISOString() }
          : { clearDueAt: true }),
        ...(this.editAssignedToUserId != null
          ? { assignedToUserId: this.editAssignedToUserId }
          : { clearAssignee: true }),
      })
      .subscribe({
        next: () => {
          this.cancelEdit();
          this.loadTasks(false);
        },
        error: () => {
          this.error = true;
          this.savingEdit = false;
        },
      });
  }

  onInvite() {
    if (!this.inviteEmail.trim() || this.inviting) {
      return;
    }
    this.inviting = true;
    this.inviteErrorMessage = '';
    this.boardApi.inviteMember(this.boardId, this.inviteEmail.trim()).subscribe({
      next: () => {
        this.inviteEmail = '';
        this.loadMembers();
        this.inviting = false;
      },
      error: (err) => {
        this.inviteErrorMessage = getApiErrorMessage(
          err,
          'No se pudo invitar al usuario.'
        );
        this.inviting = false;
      },
    });
  }

  openBoardEdit() {
    this.editingBoard = true;
    this.boardEditTitle = this.boardTitle;
    this.boardEditDescription = this.boardDescription;
    this.boardEditError = false;
  }

  cancelBoardEdit() {
    this.editingBoard = false;
    this.boardEditTitle = '';
    this.boardEditDescription = '';
    this.savingBoard = false;
    this.boardEditError = false;
  }

  saveBoardEdit() {
    if (!this.boardEditTitle.trim() || this.savingBoard) {
      return;
    }
    this.savingBoard = true;
    this.boardEditError = false;
    this.boardApi
      .updateBoard(
        this.boardId,
        this.boardEditTitle.trim(),
        this.boardEditDescription.trim() || undefined,
      )
      .subscribe({
        next: (board) => {
          this.boardTitle = board.title;
          this.boardDescription = board.description ?? '';
          this.cancelBoardEdit();
        },
        error: () => {
          this.boardEditError = true;
          this.savingBoard = false;
        },
      });
  }

  deleteBoard() {
    const ok = confirm('¿Eliminar este tablero? No se puede deshacer.');
    if (!ok) {
      return;
    }
    this.boardApi.deleteBoard(this.boardId).subscribe({
      next: () => {
        this.router.navigate(['/boards']);
      },
      error: () => {
        this.boardEditError = true;
      },
    });

  }

  get isBoardOwner(): boolean {
    const me = this.auth.getUserId();
    return (
      me != null &&
      this.boardOwnerUserId != null &&
      me === this.boardOwnerUserId
    );
  }
}
