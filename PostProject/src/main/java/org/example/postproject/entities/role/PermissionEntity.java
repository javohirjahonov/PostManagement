package org.example.postproject.entities.role;

import jakarta.persistence.Entity;
import lombok.*;
import org.example.postproject.entities.BaseEntity;

@Entity(name = "permission")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionEntity extends BaseEntity {
    private String permission;
}
