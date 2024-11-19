package org.example.postproject.api.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StandardResponse<T> {
    private Status status;
    private String message;
    private T data;

    public StandardResponse(Status status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
