package org.example.postproject.api.dtos.request;

import lombok.Data;

@Data
public class UpdatePasswordDto {
    private String email;
    private String newPassword;
}
