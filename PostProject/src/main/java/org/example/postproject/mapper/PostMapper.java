package org.example.postproject.mapper;

import org.example.postproject.api.dtos.PostUpdateDto;
import org.example.postproject.api.dtos.request.PostCreateDto;
import org.example.postproject.api.dtos.response.PostResponseDto;
import org.example.postproject.entities.post.PostEntity;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {
    public PostEntity toEntity(PostCreateDto dto) {
        PostEntity post = new PostEntity();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImage(dto.getImage());
        return post;
    }

    public PostResponseDto toResponseDto(PostEntity entity) {
        return PostResponseDto.builder()
                .id(entity.getId().toString())
                .title(entity.getTitle())
                .content(entity.getContent())
                .userId(entity.getUser().getId().toString())
                .userName(entity.getUser().getFullName())
                .image(entity.getImage())
                .build();
    }

    public void updateEntity(PostEntity entity, PostUpdateDto dto) {
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setImage(dto.getImage());
    }
}
