package com.spotify11.demo.dtos;

import java.util.List;

import com.spotify11.demo.entity.Playlist;
import com.spotify11.demo.entity.Song;
import com.spotify11.demo.entity.UploadStatus;

public record PlaylistDetailDto(Integer id, String name, String ownerName, List<TrackDto> tracks) {
    public static PlaylistDetailDto from(Playlist p) {
        var tracks = p.getTracks().stream()
                .map(TrackDto::from)
                .toList();
        // use getName() or getPlaylistName() — match your entity
        var name = p.getPlaylistName();
        return new PlaylistDetailDto(p.getId(), name, p.getOwner().getFullName(), tracks);
    }

    public record TrackDto(Integer id, String title, String artist, UploadStatus status) {
        public static TrackDto from(Song s) {
            return new TrackDto(s.getId(), s.getTitle(), s.getArtist(), s.getStatus());
        }
    }

    
}
