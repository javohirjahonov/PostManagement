package org.example.postproject.api.dtos.request;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponseForFront {
    private UUID id;
    private String name;
}
