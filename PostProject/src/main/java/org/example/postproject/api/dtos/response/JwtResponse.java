package org.example.postproject.api.dtos.response;

import lombok.*;
import org.example.postproject.api.dtos.request.UserDetailsForFront;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private UserDetailsForFront user;
}
