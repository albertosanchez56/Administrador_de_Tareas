package com.tareas.taskboard.entity;

import java.io.Serializable;
import java.util.Objects;

public class BoardMemberId implements Serializable{

    private Long board;
    private Long user;

    public BoardMemberId(){}

    public BoardMemberId(Long boardId, Long userId) {
        this.board = boardId;
        this.user = userId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BoardMemberId that = (BoardMemberId) obj;
        return Objects.equals(board, that.board) && Objects.equals(user, that.user);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(board, user);
    }
    
}
