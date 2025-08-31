package com.spotify11.demo.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.spotify11.demo.dtos.PlaylistDetailDto;
import com.spotify11.demo.dtos.TrackDto;
import com.spotify11.demo.enums.Visibility;
import com.spotify11.demo.repo.PlaylistRepo;
import com.spotify11.demo.repo.SongRepo;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class PlaylistTrackImpl implements PlaylistTrackService {
    private final PlaylistRepo playlistRepo;
    private final SongRepo songRepo;


    @Transactional  
    @Override
    public PlaylistDetailDto addSongToPlaylist(int userId, int playlistId, int songId)
            throws ResponseStatusException, AccessDeniedException {
         var p = playlistRepo.findWithTracksById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (p.getOwner().getId() != (userId))
            throw new AccessDeniedException("Not the owner");
        
        var s = songRepo.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.addTrack(s);
        return PlaylistDetailDto.from(p);
    }

    @Transactional  
    @Override
    public PlaylistDetailDto removeSongFromPlaylist(int userId, int playlistId, int songId)
            throws ResponseStatusException, AccessDeniedException {
        var p = playlistRepo.findWithTracksById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (p.getOwner().getId() != (userId))
            throw new AccessDeniedException("Not the owner");
        p.getTracks().removeIf(t -> t.getId() != (songId));
        return PlaylistDetailDto.from(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistDetailDto viewPlaylist(Integer viewerId, boolean isAdmin, int playlistId) {
        var p = playlistRepo.findWithTracksById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var pub = p.getVisibility() == Visibility.PUBLIC;

        var ownerView = viewerId != null && viewerId.equals(p.getOwner().getId());
        if (!pub && !ownerView && !isAdmin) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
         var tracks = p.getTracks().stream()
        .map(PlaylistDetailDto.TrackDto::from)
        .toList();
        return new PlaylistDetailDto(p.getId(), p.getPlaylistName(), p.getOwner().getFullName(), tracks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistDetailDto> listUserPlaylistsWithSongs(Integer viewerId, boolean isAdmin, int userId){
         boolean owner = (viewerId != null && viewerId.equals(userId));
        var playlists = owner || isAdmin
            ? playlistRepo.findByOwnerIdOrderByIdDesc(userId)                       // all playlists
            : playlistRepo.findByOwnerIdAndVisibilityOrderByIdDesc(userId, Visibility.PUBLIC); // public only
        return playlists.stream().map(PlaylistDetailDto::from).toList();
    }
    
}
