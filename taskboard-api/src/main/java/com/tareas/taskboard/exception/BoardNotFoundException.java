package com.tareas.taskboard.exception;

public class BoardNotFoundException extends RuntimeException{
    public BoardNotFoundException(String message) {
        super(message);
    }
}
