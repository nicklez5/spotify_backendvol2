package com.spotify11.demo.dtos;

public record MeDto(Integer id, String fullName, String email) {
    public static MeDto from(com.spotify11.demo.entity.User u){
        return new MeDto(u.getId(), u.getFullName(), u.getEmail());
    }
}