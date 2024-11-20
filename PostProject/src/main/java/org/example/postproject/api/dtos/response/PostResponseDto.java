package org.example.postproject.api.dtos.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDto {
    private String id;
    private String title;
    private String content;
    private String userId;
    private String userName;
    private byte[] image;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
