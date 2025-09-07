package com.spotify11.demo.services;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.spotify11.demo.dtos.PlaylistDetailDto;
import com.spotify11.demo.dtos.TrackDto;
import com.spotify11.demo.entity.Song;
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
    private final SongService songService;

    private Function<Song,String> presigner(int userId){
        return s -> songService.presignedGet(s.getId());
    }
    @Transactional  
    @Override
    public PlaylistDetailDto addSongToPlaylist(int userId, int playlistId, int songId)
            throws ResponseStatusException, AccessDeniedException {
         var p = playlistRepo.findWithTracksById(playlistId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!Objects.equals(p.getOwner().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the owner");
        }

        var s = songRepo.findById(songId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // If already present, just return (idempotent)
        if (p.getTracks().stream().anyMatch(t -> t.getId().equals(songId))) {
            return PlaylistDetailDto.from(p, presigner(userId));
        }

        p.addTrack(s);                 // safe helper with contains guard on the other side too
        // no need to save explicitly; dirty checking will persist
        return PlaylistDetailDto.from(p, presigner(userId));
    }

    @Transactional  
    @Override
    public PlaylistDetailDto removeSongFromPlaylist(int userId, int playlistId, int songId)
            throws ResponseStatusException, AccessDeniedException {
        var p = playlistRepo.findWithTracksById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (p.getOwner().getId() != (userId))
            throw new AccessDeniedException("Not the owner");
        p.getTracks().removeIf(t -> t.getId() == (songId));
        return PlaylistDetailDto.from(p,presigner(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistDetailDto viewPlaylist(Integer viewerId, boolean isAdmin, int playlistId) {
        var p = playlistRepo.findWithTracksById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var pub = p.getVisibility() == Visibility.PUBLIC;

        var ownerView = viewerId != null && viewerId.equals(p.getOwner().getId());
        if (!pub && !ownerView && !isAdmin) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        Function<Song,String> resolver = s -> songService.presignedGet(s.getId());
        return PlaylistDetailDto.from(p,resolver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistDetailDto> listUserPlaylistsWithSongs(Integer viewerId, boolean isAdmin, int userId){
         boolean owner = (viewerId != null && viewerId.equals(userId));
        var playlists = owner || isAdmin
            ? playlistRepo.findByOwnerIdOrderByIdDesc(userId)                       // all playlists
            : playlistRepo.findByOwnerIdAndVisibilityOrderByIdDesc(userId, Visibility.PUBLIC); // public only
        Function<Song,String> resolver = s -> songService.presignedGet(s.getId());  
        return playlists.stream().map(p -> PlaylistDetailDto.from(p, resolver)).toList();
    }
    
}
