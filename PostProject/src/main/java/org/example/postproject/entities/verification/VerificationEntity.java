package org.example.postproject.entities.verification;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.*;
import org.example.postproject.entities.BaseEntity;
import org.example.postproject.entities.user.UserEntity;

@Entity(name = "verification")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VerificationEntity extends BaseEntity {
    @OneToOne
    private UserEntity userId;
    private String code;
}
