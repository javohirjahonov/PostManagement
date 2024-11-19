package org.example.postproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.postproject.api.dtos.PostUpdateDto;
import org.example.postproject.api.dtos.request.PostCreateDto;
import org.example.postproject.api.dtos.response.PostResponseDto;
import org.example.postproject.api.dtos.response.StandardResponse;
import org.example.postproject.entities.user.UserEntity;
import org.example.postproject.repository.UserRepository;
import org.example.postproject.service.post.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;
    private final UserRepository userRepository;

    @PostMapping(value = "/create-post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public StandardResponse<PostResponseDto> createPost(
            @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        String email = principal.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        byte[] imageData = null;
        if (image != null) {
            try {
                imageData = image.getBytes();
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error reading image file");
            }
        }

        PostCreateDto postCreateDto = new PostCreateDto();
        postCreateDto.setTitle(title);
        postCreateDto.setContent(content);
        postCreateDto.setImage(imageData);

        return postService.savePost(postCreateDto, user.getId());
    }


    @PutMapping("/update-post/{postId}")
    @PreAuthorize("@postSecurityService.isAuthor(#postId, principal.name)")
    public StandardResponse<PostResponseDto> updatePost(
            @PathVariable UUID postId,
            @RequestBody PostUpdateDto postUpdateDto,
            Principal principal
    ) {
        String email = principal.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return postService.updatePost(postId, postUpdateDto, user.getId());
    }

    @DeleteMapping("/delete-post/{postId}")
    @PreAuthorize("@postSecurityService.isAuthor(#postId, principal.name)")
    public void deletePost(@PathVariable UUID postId, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        postService.deletePost(postId, userId);
    }

    @GetMapping("/get-user-post/{postId}")
    @PreAuthorize("permitAll()") // Public access
    public StandardResponse<PostResponseDto> getPostById(@PathVariable UUID postId) {
        return postService.getPostById(postId);
    }

    @GetMapping("/get-all-posts")
    @PreAuthorize("permitAll()") // Public access
    public StandardResponse<List<PostResponseDto>> getAllPosts() {
        return postService.getAllPosts();
    }
}
