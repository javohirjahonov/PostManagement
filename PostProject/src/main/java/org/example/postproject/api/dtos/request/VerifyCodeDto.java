package org.example.postproject.api.dtos.request;

import lombok.Data;

@Data
public class VerifyCodeDto {
    private String email;
    private String code;
}
