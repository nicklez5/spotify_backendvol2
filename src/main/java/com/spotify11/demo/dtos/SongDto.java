package com.spotify11.demo.dtos;

import com.spotify11.demo.entity.Song;
import com.spotify11.demo.entity.UploadStatus;

public record SongDto(Integer id, String title, String artist, String streamUrl, Long sizeBytes, String contentType, Integer durationSec, UploadStatus status){
    public static SongDto from(Song s, String publicUrlOrNull){
        return new SongDto(s.getId(), s.getTitle(), s.getArtist(), publicUrlOrNull, s.getSizeBytes(), s.getContentType(), s.getDurationSec(), s.getStatus());
    }
}