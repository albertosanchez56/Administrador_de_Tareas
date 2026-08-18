package com.tareas.taskboard.service;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

import com.tareas.taskboard.dto.BoardMemberResponse;
import com.tareas.taskboard.dto.BoardResponse;
import com.tareas.taskboard.dto.CreateBoardRequest;
import com.tareas.taskboard.dto.InviteMemberRequest;
import com.tareas.taskboard.dto.UpdateBoardRequest;
import com.tareas.taskboard.entity.Board;
import com.tareas.taskboard.entity.BoardMemberId;
import com.tareas.taskboard.entity.BoardMembers;
import com.tareas.taskboard.entity.BoardMembers.Role;
import com.tareas.taskboard.entity.Task;
import com.tareas.taskboard.entity.User;
import com.tareas.taskboard.exception.AccessDeniedException;
import com.tareas.taskboard.exception.BoardNotFoundException;
import com.tareas.taskboard.exception.MemberAlreadyExistsException;
import com.tareas.taskboard.exception.MemberNotFoundException;
import com.tareas.taskboard.exception.UserNotFoundException;
import com.tareas.taskboard.repository.BoardMemberRepository;
import com.tareas.taskboard.repository.BoardRepository;
import com.tareas.taskboard.repository.TaskRepository;
import com.tareas.taskboard.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final TaskRepository taskRepository;

    public BoardService(BoardRepository boardRepository, UserRepository userRepository,
            BoardMemberRepository boardMemberRepository, TaskRepository taskRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.boardMemberRepository = boardMemberRepository;
        this.taskRepository = taskRepository;
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

    // Devuelve los boards donde el usuario participa (OWNER o MEMBER) según
    // board_members.
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

    // Añade un miembro al board. Solo el owner puede invitar; requesterUserId viene
    // del JWT.
    @Transactional // necesario por relaciones LAZY al mapear BoardMemberResponse
    public BoardMemberResponse addMember(Long boardId, InviteMemberRequest request, Long requesterUserId) {
        // Compruebo que el board existe.
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        // Solo el dueño del tablero puede invitar a otros usuarios.
        if (!board.getOwner().getId().equals(requesterUserId)) {
            throw new AccessDeniedException("You are not allowed to add members to this board");
        }

        // Busco al usuario invitado por email (debe estar registrado en la app).
        User invitedUser = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Evito duplicados: el owner ya está en board_members y un member no se invita
        // dos veces.
        if (boardMemberRepository.existsByBoardAndUser(board, invitedUser)) {
            throw new MemberAlreadyExistsException("User already a member of this board");
        }

        BoardMembers boardMember = new BoardMembers(board, invitedUser, Role.MEMBER);
        BoardMembers saved = boardMemberRepository.save(boardMember);

        return BoardMemberResponse.from(saved);
    }

    @Transactional
    public List<BoardMemberResponse> getBoardMembers(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        if (!board.getOwner().getId().equals(userId) && !boardMemberRepository.existsByBoardAndUser(board,
                userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found")))) {
            throw new AccessDeniedException("You are not allowed to get the members of this board");
        }

        List<BoardMembers> members = boardMemberRepository.findByBoard(board);

        return members.stream()
                .map(BoardMemberResponse::from)
                .toList();
    }

    @Transactional
    public void removeMember(Long boardId, Long memberId, Long requesterUserId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        if (!board.getOwner().getId().equals(requesterUserId)) {
            throw new AccessDeniedException("You are not allowed to remove members from this board");
        }

        if (board.getOwner().getId().equals(memberId)) {
            throw new AccessDeniedException("Cannot remove the board owner");
        }

        BoardMembers member = boardMemberRepository
                .findById(new BoardMemberId(boardId, memberId))
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        List<Task> tasks = taskRepository.findByBoardAndAssignedTo_Id(board, memberId);
        for (Task task : tasks) {
            task.setAssignedTo(null);
            task.setUpdatedAt(Instant.now());
        }
        taskRepository.saveAll(tasks);

        boardMemberRepository.delete(member);
    }

    @Transactional
    public BoardResponse getBoard(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        if (!board.getOwner().getId().equals(userId) && !boardMemberRepository.existsByBoardAndUser(board,
                userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found")))) {
            throw new AccessDeniedException("You are not allowed to access this board");
        }

        return BoardResponse.from(board);
    }

    @Transactional
    public BoardResponse updateBoard(Long boardId, UpdateBoardRequest request, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        if (!board.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to update/delete this board");
        }

        board.setTitle(request.title());
        board.setDescription(request.description());
        board.setUpdatedAt(Instant.now());

        Board updated = boardRepository.save(board);
        return BoardResponse.from(updated);
    }

    @Transactional
    public void deleteBoard(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        if (!board.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to update/delete this board");
        }

        boardRepository.delete(board);
    }
}
