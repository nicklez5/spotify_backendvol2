package com.spotify11.demo.services;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import com.spotify11.demo.dtos.PlaylistDetailDto;

import jakarta.transaction.Transactional;

public interface PlaylistTrackService {
    @Transactional
    PlaylistDetailDto addSongToPlaylist(int userId, int playlistId, int songId) throws ResponseStatusException, AccessDeniedException;

    @Transactional
    PlaylistDetailDto removeSongFromPlaylist(int userId, int playlistId, int songId) throws ResponseStatusException, AccessDeniedException;

    @Transactional
    PlaylistDetailDto viewPlaylist(Integer viewerId, boolean isAdmin, int playlistId );

    @Transactional
    List<PlaylistDetailDto> listUserPlaylistsWithSongs(Integer viewerId, boolean isAdmin,int userId );
}
