package org.example.postproject.service.postSecurityService;

import org.example.postproject.entities.post.PostEntity;
import org.example.postproject.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PostSecurityService {
    private final PostRepository postRepository;

    public PostSecurityService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public boolean isAuthor(UUID postId, String username) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return post.getUser().getEmail().equals(username);
    }
}
