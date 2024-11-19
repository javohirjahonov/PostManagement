package org.example.postproject.api.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostResponseDto {
    private String id;
    private String title;
    private String content;
    private String userId;
    private String userName;
    private byte[] image;
}
