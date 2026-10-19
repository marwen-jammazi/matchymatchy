package com.moviematch.backend.exception;

public class ResourceAccessDeniedException extends RuntimeException {

    public ResourceAccessDeniedException(String message) {
        super(message);
    }
}