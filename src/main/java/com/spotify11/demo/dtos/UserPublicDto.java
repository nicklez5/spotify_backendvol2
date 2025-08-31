package com.spotify11.demo.dtos;

import java.util.Date;

import com.spotify11.demo.entity.User;

public record UserPublicDto(Integer id, String fullName, Date createdAt, int playlistCount) {
    public static UserPublicDto from(User u, int count) {
        return new UserPublicDto(u.getId(), u.getFullName(), u.getCreatedAt(), count);
    }
}