package com.spotify11.demo.dtos;

import java.util.List;
import java.util.function.Function;

import com.spotify11.demo.entity.Playlist;
import com.spotify11.demo.entity.Song;
import com.spotify11.demo.entity.UploadStatus;

public record PlaylistDetailDto(Integer id, String name, String ownerName, List<TrackDto> tracks) {
    public static PlaylistDetailDto from(Playlist p, Function<Song, String> streamUrl) {
        var tracks = p.getTracks().stream()
                .map( s -> TrackDto.from(s, streamUrl.apply(s)))
                .toList();
        // use getName() or getPlaylistName() — match your entity
        var name = p.getPlaylistName();
        return new PlaylistDetailDto(p.getId(), name, p.getOwner().getFullName(), tracks);
    }

    public record TrackDto(Integer id, String title, String artist, String streamUrl, Long sizeBytes, String contentType, UploadStatus status) {
        public static TrackDto from(Song s, String streamUrl) {
            return new TrackDto(s.getId(), s.getTitle(), s.getArtist(), streamUrl, s.getSizeBytes(), s.getContentType(), s.getStatus());
        }
    }

    
}
