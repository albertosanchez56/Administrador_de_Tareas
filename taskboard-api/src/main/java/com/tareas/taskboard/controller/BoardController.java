package com.tareas.taskboard.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tareas.taskboard.dto.BoardMemberResponse;
import com.tareas.taskboard.dto.BoardResponse;
import com.tareas.taskboard.dto.CreateBoardRequest;
import com.tareas.taskboard.dto.InviteMemberRequest;
import com.tareas.taskboard.service.BoardService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@Valid @RequestBody CreateBoardRequest request) {
        BoardResponse response = boardService.createBoard(request, getAuthenticatedUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BoardResponse>> getMyBoards() {
        List<BoardResponse> response = boardService.getMyBoards(getAuthenticatedUserId());
        return ResponseEntity.ok(response);
    }

    private Long getAuthenticatedUserId() {
        return Long.valueOf(
                SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @GetMapping("/{boardId}/members")
    public ResponseEntity<List<BoardMemberResponse>> getBoardMembers(@PathVariable Long boardId) {
        List<BoardMemberResponse> response = boardService.getBoardMembers(boardId, getAuthenticatedUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{boardId}/members")
    public ResponseEntity<BoardMemberResponse> addBoardMember(@PathVariable Long boardId,
            @Valid @RequestBody InviteMemberRequest request) {
        BoardMemberResponse response = boardService.addMember(boardId, request, getAuthenticatedUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{boardId}/members/{memberId}")
    public ResponseEntity<Void> removeBoardMember(
            @PathVariable Long boardId,
            @PathVariable Long memberId) {
        boardService.removeMember(boardId, memberId, getAuthenticatedUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable Long boardId) {
        BoardResponse response = boardService.getBoard(boardId, getAuthenticatedUserId());
        return ResponseEntity.ok(response);
    }
}
