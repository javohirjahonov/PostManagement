package org.example.postproject.api.dtos.request;

import lombok.*;
import org.example.postproject.entities.gender.Gender;
import org.example.postproject.entities.user.UserState;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserDetailsForFront {
    private UUID id;
    private String fullName;
    private String email;
    private UserState userState;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private List<String> roles;
    private List<String> permissions;

}
