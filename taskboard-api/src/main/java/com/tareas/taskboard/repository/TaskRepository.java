package com.tareas.taskboard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tareas.taskboard.entity.Board;
import com.tareas.taskboard.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByBoard(Board board);
}
