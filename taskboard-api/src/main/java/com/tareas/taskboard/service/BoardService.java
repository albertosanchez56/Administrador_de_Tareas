package com.tareas.taskboard.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.tareas.taskboard.dto.BoardResponse;
import com.tareas.taskboard.dto.CreateBoardRequest;
import com.tareas.taskboard.entity.Board;
import com.tareas.taskboard.entity.BoardMembers;
import com.tareas.taskboard.entity.BoardMembers.Role;
import com.tareas.taskboard.entity.User;
import com.tareas.taskboard.exception.UserNotFoundException;
import com.tareas.taskboard.repository.BoardMemberRepository;
import com.tareas.taskboard.repository.BoardRepository;
import com.tareas.taskboard.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardMemberRepository boardMemberRepository;

    public BoardService(BoardRepository boardRepository, UserRepository userRepository,
            BoardMemberRepository boardMemberRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.boardMemberRepository = boardMemberRepository;
    }

    // Crea un board nuevo. El ownerUserId viene del JWT (SecurityContext en el
    // controller).
    @Transactional
    public BoardResponse createBoard(CreateBoardRequest request, Long ownerUserId) {
        // Busco al usuario que será dueño del tablero.
        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        // Creo la entidad con los datos del DTO + el owner.
        Board board = new Board(request.title(), request.description(), owner);

        // Guardo en BD y devuelvo DTO (no expongo la entidad al controller).
        Board saved = boardRepository.save(board);

        // Creo el board member para el owner.
        BoardMembers boardMember = new BoardMembers(saved, owner, Role.OWNER);
        boardMemberRepository.save(boardMember);

        return BoardResponse.from(saved);
    }

    // Devuelve los boards donde el usuario participa (OWNER o MEMBER) según board_members.
    // userId viene del JWT en el controller.
    @Transactional
    public List<BoardResponse> getMyBoards(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<BoardMembers> memberships = boardMemberRepository.findByUser(user);

        return memberships.stream()
                .map(BoardMembers::getBoard)
                .map(BoardResponse::from)
                .toList();
    }
}
