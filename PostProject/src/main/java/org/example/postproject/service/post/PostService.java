package org.example.postproject.service.post;

import jakarta.persistence.EntityNotFoundException;
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
import java.util.stream.Collectors;

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

        // Handle null content
        if (postCreateDto.getContent() == null || postCreateDto.getContent().isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }

        PostEntity post = postMapper.toEntity(postCreateDto);
        post.setUser(user);
        postRepository.save(post);

        return StandardResponse.<PostResponseDto>builder()
                .status(Status.SUCCESS)
                .data(postMapper.toResponseDto(post))
                .message("Post saved successfully")
                .build();
    }

    public StandardResponse<PostResponseDto> updatePost(UUID postId, PostUpdateDto dto, UUID userId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not authorized to update this post");
        }

        postMapper.updateEntity(post, dto);
        postRepository.save(post);

        return StandardResponse.<PostResponseDto>builder()
                .status(Status.SUCCESS)
                .data(postMapper.toResponseDto(post))
                .message("Post update Successfully")
                .build();
    }

    public void deletePost(UUID postId, UUID userId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
    }

    public StandardResponse<PostResponseDto> getPostById(UUID postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        return StandardResponse.<PostResponseDto>builder()
                .status(Status.SUCCESS)
                .data(postMapper.toResponseDto(post))
                .message("User posts")
                .build();
    }

    public StandardResponse<List<PostResponseDto>> getAllPosts() {
        return StandardResponse.<List<PostResponseDto>>builder()
                .status(Status.SUCCESS)
                .data(postRepository.findAll().stream()
                        .map(postMapper::toResponseDto)
                        .collect(Collectors.toList()))
                .message("All posts")
                .build();
    }

    public UUID getPostAuthorId(UUID postId) {
        return postRepository.findById(postId)
                .map(PostEntity::getId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }
}
