package com.spotify11.demo.services;

import com.spotify11.demo.dtos.CreateSongDto;
import com.spotify11.demo.dtos.SongDto;
import com.spotify11.demo.dtos.TrackDto;
import com.spotify11.demo.entity.Song;

import com.spotify11.demo.exception.MentionedFileNotFoundException;
import com.spotify11.demo.exception.SongException;

import com.spotify11.demo.exception.UserException;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SongService {



    SongDto create(Integer ownerId, CreateSongDto dto);

    Map<String, String> presignUpload(Integer ownerId, Integer songId, String contentType);
    List<TrackDto> listTracksForPlaylist(Integer ownerId, Integer playlistId);

    void deleteSong(Integer ownerId, Integer songId) throws RuntimeException;
    
    SongDto finalizeUpload(Integer ownerId, Integer songId, Long sizeBytes) throws RuntimeException;

    String presignedGet(Integer ownerId, Integer songId) throws RuntimeException;
    
    Song getSong(int id, String email) throws  UserException,SongException;
    Song getSong(String title, String email) throws  UserException,SongException;
    List<Song> getAllSongs(String email) throws UserException, SongException;
}
