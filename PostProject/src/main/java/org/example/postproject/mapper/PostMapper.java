package org.example.postproject.mapper;

import org.example.postproject.api.dtos.request.PostCreateDto;
import org.example.postproject.api.dtos.response.PostResponseDto;
import org.example.postproject.entities.post.PostEntity;
import org.example.postproject.entities.user.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostMapper {
    public PostEntity toEntity(PostCreateDto postCreateDto, UserEntity user) {
        PostEntity postEntity = new PostEntity();
        postEntity.setTitle(postCreateDto.getTitle());
        postEntity.setContent(postCreateDto.getContent());
        postEntity.setImage(postCreateDto.getImage());
        postEntity.setUser(user);
        return postEntity;
    }

    public PostResponseDto toResponseDto(PostEntity postEntity) {
        PostResponseDto responseDto = new PostResponseDto();
        responseDto.setId(String.valueOf(postEntity.getId()));
        responseDto.setTitle(postEntity.getTitle());
        responseDto.setContent(postEntity.getContent());
        responseDto.setImage(Base64.getEncoder().encodeToString(postEntity.getImage()).getBytes());
        if (postEntity.getUser() != null) {
            responseDto.setUserId(String.valueOf(postEntity.getUser().getId()));
            responseDto.setUserName(postEntity.getUser().getUsername());
        }
        return responseDto;
    }

    public List<PostResponseDto> toResponseDtoList(List<PostEntity> postEntities) {
        return postEntities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}
