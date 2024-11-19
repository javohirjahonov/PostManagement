package org.example.postproject.entities.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.postproject.entities.BaseEntity;
import org.example.postproject.entities.post.PostEntity;

import java.util.List;
@Entity(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostEntity> userPosts;
}
