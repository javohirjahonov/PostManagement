package org.example.postproject.api.dtos.response;

import org.springframework.http.HttpStatus;

public record ApiErrorResponse(
        String message,
        HttpStatus httpStatus,
        int code
) {
}
