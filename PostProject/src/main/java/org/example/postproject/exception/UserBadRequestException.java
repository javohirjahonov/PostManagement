package org.example.postproject.exception;

public class UserBadRequestException extends RuntimeException{
    public UserBadRequestException(String message) {
        super(message);
    }
}
