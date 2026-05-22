package com.tareas.taskboard.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tareas.taskboard.entity.Board;
import com.tareas.taskboard.entity.User;
import com.tareas.taskboard.repository.BoardRepository;

@Service
public class BoardService {
    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }
    
    public Board createBoard(Board board) {
        return boardRepository.save(board);
    }

    public List<Board> getBoardsByOwner(User owner) {
        return boardRepository.findByOwner(owner);
    }
}
