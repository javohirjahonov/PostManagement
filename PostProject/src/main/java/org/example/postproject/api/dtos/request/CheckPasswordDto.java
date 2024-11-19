package org.example.postproject.api.dtos.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CheckPasswordDto {
    private String password;
}
