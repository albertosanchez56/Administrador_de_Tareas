package com.tareas.taskboard.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tareas.taskboard.dto.BoardResponse;
import com.tareas.taskboard.dto.CreateBoardRequest;
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
            SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }
}
