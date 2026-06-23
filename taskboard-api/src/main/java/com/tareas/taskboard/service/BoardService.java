package com.tareas.taskboard.service;


import java.util.List;
import org.springframework.stereotype.Service;

import com.tareas.taskboard.dto.BoardResponse;
import com.tareas.taskboard.dto.CreateBoardRequest;
import com.tareas.taskboard.entity.Board;
import com.tareas.taskboard.entity.User;
import com.tareas.taskboard.repository.BoardRepository;
import com.tareas.taskboard.repository.UserRepository;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public BoardService(BoardRepository boardRepository, UserRepository userRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    // Crea un board nuevo. El ownerUserId viene del JWT (SecurityContext en el controller).
    public BoardResponse createBoard(CreateBoardRequest request, Long ownerUserId) {
        // Busco al usuario que será dueño del tablero.
        User owner = userRepository.findById(ownerUserId)
            .orElseThrow(() -> new RuntimeException("Owner not found"));

        // Creo la entidad con los datos del DTO + el owner.
        Board board = new Board(request.title(), request.description(), owner);

        // Guardo en BD y devuelvo DTO (no expongo la entidad al controller).
        Board saved = boardRepository.save(board);
        return BoardResponse.from(saved);
    }

    // Devuelve los boards donde el usuario es owner.
    // De momento solo miro boards.owner_user_id; luego miraré también board_members.
    public List<BoardResponse> getMyBoards(Long ownerUserId) {
        User owner = userRepository.findById(ownerUserId)
            .orElseThrow(() -> new RuntimeException("Owner not found"));

        List<Board> boards = boardRepository.findByOwner(owner);

        // Convierto cada Board a BoardResponse para la API.
        return boards.stream()
            .map(BoardResponse::from).toList();
    }
}
