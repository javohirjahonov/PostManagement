package org.example.postproject.entities.role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.*;
import org.example.postproject.entities.BaseEntity;

import java.util.List;

@Entity(name = "role_entity")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RoleEntity extends BaseEntity {
    private String name;
    @ManyToMany
    @JsonIgnore
    private List<PermissionEntity> permissions;
}
