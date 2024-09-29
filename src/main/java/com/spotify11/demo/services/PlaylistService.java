package com.spotify11.demo.services;


import com.spotify11.demo.entity.Playlist;


import com.spotify11.demo.entity.Song;
import com.spotify11.demo.exception.SongException;
import com.spotify11.demo.exception.UserException;
import jakarta.transaction.Transactional;

import java.util.List;

public interface PlaylistService {

    String getPlaylistName(String email) throws UserException;
    @Transactional
    Playlist addSong(int song_id, String email) throws Exception;
    List<Song> getSongs(String email) throws UserException;


    @Transactional
    Playlist removeSong(int song_id, String email) throws SongException, UserException;

    String renamePlaylist(String email, String playlist_name) throws UserException;
    Playlist clearPlaylist(String email) throws UserException;


}
