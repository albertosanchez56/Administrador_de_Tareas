package com.tareas.taskboard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tareas.taskboard.entity.Board;
import com.tareas.taskboard.entity.BoardInvitation;
import com.tareas.taskboard.entity.User;

public interface BoardInvitationRepository extends JpaRepository<BoardInvitation, Long> {

    boolean existsByBoardAndInviteeAndStatus(Board board, User invitee, BoardInvitation.Status status);

    List<BoardInvitation> findByInviteeAndStatus(User invitee, BoardInvitation.Status status);

    List<BoardInvitation> findByBoardAndStatus(Board board, BoardInvitation.Status status);
}
