package com.tareas.taskboard.service;


import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tareas.taskboard.dto.CreateTaskRequest;
import com.tareas.taskboard.entity.Board;
import com.tareas.taskboard.entity.Task;
import com.tareas.taskboard.entity.User;
import com.tareas.taskboard.dto.TaskResponse;
import com.tareas.taskboard.dto.UpdateTaskStatusRequest;
import com.tareas.taskboard.repository.BoardRepository;
import com.tareas.taskboard.repository.TaskRepository;
import com.tareas.taskboard.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, BoardRepository boardRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    // Crea una tarea dentro de un board. userId viene del JWT.
    @Transactional // necesario por open-in-view=false y relaciones LAZY al mapear el DTO
    public TaskResponse createTask(CreateTaskRequest request, Long boardId, Long userId) {
        // Busca el board y comprueba que el usuario autenticado es el owner.
        Board board = getBoardIfOwner(boardId, userId);

        // Busco al usuario que crea la tarea (el autenticado).
        User creator = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Owner not found"));

        // Constructor simple: status TODO y position 0 los pone la entidad.
        Task task = new Task(board, request.title(), request.description(), creator);
        Task saved = taskRepository.save(task);

        // Mapeo a DTO dentro de la transacción (evita problemas con relaciones LAZY).
        return TaskResponse.fromTask(saved);
    }

    // Lista las tareas de un board concreto.
    @Transactional // igual que createTask: leo relaciones LAZY al convertir a TaskResponse
    public List<TaskResponse> getTasksByBoard(Long boardId, Long userId) {
        Board board = getBoardIfOwner(boardId, userId);

        List<Task> tasks = taskRepository.findByBoard(board);

        // Convierto cada Task a DTO para no exponer la entidad.

        return tasks.stream()
            .map(TaskResponse::fromTask)
            .toList();
    }
    // Cambia el status de una tarea (TODO/DOING/DONE). Tipo "mover tarjeta" en Trello.
    @Transactional
    public TaskResponse updateTaskStatus(Long boardId, Long taskId, UpdateTaskStatusRequest request, Long userId) {
        Board board = getBoardIfOwner(boardId, userId);

        // Busco la task dentro de ese board (evita actualizar una task de otro tablero).
        Task task = taskRepository.findByIdAndBoard(taskId, board)
            .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(request.status());
        task.setUpdatedAt(Instant.now());

        Task saved = taskRepository.save(task);
        return TaskResponse.fromTask(saved);
    }

    // Método privado para no repetir la misma lógica en create y list.
    // Devuelve el board solo si existe y el userId es el owner.
    private Board getBoardIfOwner(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new RuntimeException("Board not found"));

        // De momento solo el owner tiene acceso; luego ampliaré con board_members.
        if (!board.getOwner().getId().equals(userId)) {
            throw new RuntimeException("User is not the owner of the board");
        }

        return board;
    }

    
}
