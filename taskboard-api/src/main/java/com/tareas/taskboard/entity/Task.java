package com.tareas.taskboard.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tasks")
public class Task {

    public enum TaskStatus {
        TODO, DOING, DONE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;

    @Column(name = "position", nullable = false)
    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "due_at")
    private Instant dueAt;

    protected Task() {
    }

    // Para crear una tarea nueva desde la API
    public Task(Board board, String title, String description, User createdBy) {
        this.board = board;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.status = TaskStatus.TODO; // default de la BD
        this.position = 0; // default de la BD
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        // assignedTo y dueAt quedan null
    }

    public Task(Board board, String title, String description, TaskStatus status, int position, User createdBy,
            User assignedTo, Instant dueAt) {
        this.board = board;
        this.title = title;
        this.description = description;
        this.status = status;
        this.position = position;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
        this.dueAt = dueAt;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
