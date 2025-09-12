package com.spotify11.demo.services;

import com.spotify11.demo.dtos.CreateSongDto;
import com.spotify11.demo.dtos.PlaylistDetailDto;
import com.spotify11.demo.dtos.SongDto;
import com.spotify11.demo.dtos.TrackDto;
import com.spotify11.demo.entity.Song;

import com.spotify11.demo.exception.MentionedFileNotFoundException;
import com.spotify11.demo.exception.SongException;

import com.spotify11.demo.exception.UserException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SongService {


    @Transactional
    SongDto create(Integer ownerId, CreateSongDto dto) throws UserException;

  

    Map<String, String> presignUpload(Integer ownerId, Integer songId, String contentType);
    List<TrackDto> listTracksForPlaylist(Integer ownerId, Integer playlistId);

    @Transactional
    void deleteSong(Integer ownerId, Integer songId) throws RuntimeException;
    
    SongDto finalizeUpload(Integer ownerId, Integer songId, Long sizeBytes, Integer durationSec) throws RuntimeException;
    @Transactional
    String presignedGet(Integer songId) throws RuntimeException;

    Song getSong(int id, String email) throws  UserException,SongException;
    Song getSong(String title, String email) throws  UserException,SongException;
    List<TrackDto> getAllSongsFromUser(String email) throws UserException;
    List<TrackDto> getAllSongs(String email) throws UserException;
}
