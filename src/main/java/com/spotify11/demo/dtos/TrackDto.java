package com.spotify11.demo.dtos;

import com.spotify11.demo.entity.Song;
import com.spotify11.demo.entity.UploadStatus;

public record TrackDto(
    Integer id,
    String title,
    String artist,
    String streamUrl,     // presigned GET or public URL; may be null if not READY
    Long sizeBytes,
    String contentType,
    UploadStatus status
) {
  public static TrackDto from(Song s, String streamUrl) {
    return new TrackDto(
        s.getId(),
        s.getTitle(),
        s.getArtist(),
        streamUrl,                 // pass in computed URL (or null)
        s.getSizeBytes(),
        s.getContentType(),
        s.getStatus()
    );
  }
  
}
