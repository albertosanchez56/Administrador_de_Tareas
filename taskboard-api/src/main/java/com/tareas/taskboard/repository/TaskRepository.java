package com.tareas.taskboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tareas.taskboard.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
    
}
