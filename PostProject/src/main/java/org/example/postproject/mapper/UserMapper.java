package org.example.postproject.mapper;

import org.example.postproject.api.dtos.request.UserDetailsForFront;
import org.example.postproject.entities.user.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserDetailsForFront toDto(UserEntity userEntity) {
        return UserDetailsForFront.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .phoneNumber(userEntity.getPhoneNumber())
                .fullName(userEntity.getFullName())
                .build();
    }

    public List<UserDetailsForFront> toDtoList(List<UserEntity> userEntities) {
        return userEntities.stream().map(this::toDto).collect(Collectors.toList());
    }
}
