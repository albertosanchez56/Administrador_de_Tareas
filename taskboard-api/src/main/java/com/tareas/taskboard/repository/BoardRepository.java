package com.tareas.taskboard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tareas.taskboard.entity.Board;
import com.tareas.taskboard.entity.User;

public interface BoardRepository extends JpaRepository<Board, Long> {
    
    List<Board> findByOwner(User owner);
}
