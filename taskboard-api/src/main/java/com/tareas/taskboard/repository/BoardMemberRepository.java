package com.tareas.taskboard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tareas.taskboard.entity.BoardMembers;
import com.tareas.taskboard.entity.User;
import com.tareas.taskboard.entity.BoardMemberId;

public interface BoardMemberRepository extends JpaRepository<BoardMembers, BoardMemberId> {
    
    List<BoardMembers> findByUser(User user);
}
