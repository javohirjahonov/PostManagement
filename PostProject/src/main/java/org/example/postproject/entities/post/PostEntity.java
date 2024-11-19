package org.example.postproject.entities.post;

import jakarta.persistence.*;
import lombok.*;
import org.example.postproject.entities.BaseEntity;
import org.example.postproject.entities.user.UserEntity;

@Entity(name = "posts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PostEntity extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Lob
    private byte[] image;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Ensure non-null author
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

}
