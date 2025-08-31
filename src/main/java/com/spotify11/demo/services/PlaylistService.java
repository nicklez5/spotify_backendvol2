package com.spotify11.demo.services;


import com.spotify11.demo.dtos.CreatePlaylistDto;
import com.spotify11.demo.dtos.PlaylistDto;
import com.spotify11.demo.entity.Playlist;


import com.spotify11.demo.entity.Song;
import com.spotify11.demo.exception.SongException;
import com.spotify11.demo.exception.UserException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;


public interface PlaylistService {

    String getPlaylistName(String email, int id) throws UserException;

    
    @Transactional
    PlaylistDto addPlaylist(Integer userId, CreatePlaylistDto dto) throws EntityNotFoundException;

    @Transactional
    void RemovePlaylist(int userId, int playlistId) throws EntityNotFoundException, AccessDeniedException, UserException;

    @Transactional
    List<PlaylistDto> listUserPlaylists(int userId);

    @Transactional
    PlaylistDto rename(int userId, int playlistId, String newName) throws EntityNotFoundException, AccessDeniedException, UserException;

    PlaylistDto updateCover(Integer ownerId, Integer playlistId, MultipartFile file) throws RuntimeException, IOException;

    PlaylistDto clearPlaylist(String email, int id) throws UserException;

    //PlaylistDto updateCover(Integer ownerId, Integer playlistId, MultipartFile file) throws IOException;
}
