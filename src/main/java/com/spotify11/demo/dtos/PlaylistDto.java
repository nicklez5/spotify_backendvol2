package com.spotify11.demo.dtos;

import com.spotify11.demo.entity.Playlist;
public record PlaylistDto(int id, String name, String ownerName, String visibility, String coverUrl) {
    public static PlaylistDto from(Playlist p) { 
        return new PlaylistDto(p.getId(), 
                               p.getPlaylistName(), 
                               p.getOwner().getFullName(), 
                               p.getVisibility().name(), 
                               p.getCoverUrl() );
    }
}

