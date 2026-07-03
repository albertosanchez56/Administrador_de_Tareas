package com.tareas.taskboard.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tareas.taskboard.dto.CreateTaskRequest;
import com.tareas.taskboard.dto.TaskResponse;
import com.tareas.taskboard.dto.UpdateTaskStatusRequest;
import com.tareas.taskboard.service.TaskService;

import jakarta.validation.Valid;

// Rutas anidadas: las tareas siempre van dentro de un board concreto.
@RestController
@RequestMapping("/api/boards/{boardId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // POST /api/boards/{boardId}/tasks
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long boardId,
            @Valid @RequestBody CreateTaskRequest request) {
        // boardId viene de la URL; userId del JWT (no del body por seguridad).
        TaskResponse response = taskService.createTask(request, boardId, getAuthenticatedUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/boards/{boardId}/tasks
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasksByBoard(@PathVariable Long boardId) {
        List<TaskResponse> response = taskService.getTasksByBoard(boardId, getAuthenticatedUserId());
        return ResponseEntity.ok(response);
    }

    // Mismo helper que en BoardController: el JWT guarda el userId en el principal.
    private Long getAuthenticatedUserId() {
        return Long.valueOf(
                SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateStatus(@PathVariable Long boardId, @PathVariable Long taskId,@Valid @RequestBody UpdateTaskStatusRequest request){
        TaskResponse response = taskService.updateTaskStatus(boardId, taskId, request, getAuthenticatedUserId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
