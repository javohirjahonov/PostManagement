package org.example.postproject.service.post;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.postproject.api.dtos.PostUpdateDto;
import org.example.postproject.api.dtos.request.PostCreateDto;
import org.example.postproject.api.dtos.response.PostResponseDto;
import org.example.postproject.api.dtos.response.StandardResponse;
import org.example.postproject.api.dtos.response.Status;
import org.example.postproject.entities.post.PostEntity;
import org.example.postproject.entities.user.UserEntity;
import org.example.postproject.mapper.PostMapper;
import org.example.postproject.repository.PostRepository;
import org.example.postproject.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;

    @Transactional
    public StandardResponse<PostResponseDto> savePost(PostCreateDto postCreateDto, UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        PostEntity post = postMapper.toEntity(postCreateDto, user);
        post.setUser(user);

        postRepository.save(post);

        return StandardResponse.<PostResponseDto>builder()
                .status(Status.SUCCESS)
                .data(postMapper.toResponseDto(post))
                .message("Post created successfully")
                .build();
    }

        @Transactional
        public StandardResponse<PostResponseDto> updatePost(UUID postId, PostUpdateDto postUpdateDto, UUID userId) {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            PostEntity post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Post not found"));

            post.setTitle(postUpdateDto.getTitle());
            post.setContent(postUpdateDto.getContent());
            post.setUser(user);
            postRepository.save(post);

            return StandardResponse.<PostResponseDto>builder()
                    .status(Status.SUCCESS)
                    .data(postMapper.toResponseDto(post))
                    .message("Post updated successfully")
                    .build();
        }

        @Transactional
        public void deletePost(UUID postId, UUID userId) {
            PostEntity post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Post not found"));

            postRepository.delete(post);
        }

        public StandardResponse<PostResponseDto> getPostById(UUID postId) {
            PostEntity post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Post not found"));

            return StandardResponse.<PostResponseDto>builder()
                    .status(Status.SUCCESS)
                    .data(postMapper.toResponseDto(post))
                    .build();
        }

        public StandardResponse<List<PostResponseDto>> getAllPosts() {
            List<PostEntity> posts = postRepository.findAll();
            return StandardResponse.<List<PostResponseDto>>builder()
                    .status(Status.SUCCESS)
                    .data(postMapper.toResponseDtoList(posts))
                    .build();
        }

}
