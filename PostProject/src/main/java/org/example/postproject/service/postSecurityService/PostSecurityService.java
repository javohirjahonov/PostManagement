package org.example.postproject.service.postSecurityService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.postproject.entities.post.PostEntity;
import org.example.postproject.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("postSecurityService")
@RequiredArgsConstructor
public class PostSecurityService {
    private final PostRepository postRepository;

    public boolean isAuthor(UUID postId, String userId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        return post.getUser().getId().toString().equals(userId);
    }


}
