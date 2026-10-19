package com.moviematch.backend.exception;

public class DuplicateWatchRoomException extends RuntimeException {

    public DuplicateWatchRoomException(String message) {
        super(message);
    }
}