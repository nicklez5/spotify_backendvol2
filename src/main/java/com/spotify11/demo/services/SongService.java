package com.spotify11.demo.services;

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

public interface SongService {


    @Transactional
    Song updateSong(String title, String artist, int song_id, String email) throws UserException;

    Resource loadFileAsResource (String filename) throws FileNotFoundException, MentionedFileNotFoundException;
    File multipartFile(MultipartFile file, String fileName) throws IOException;


    @Transactional
    Song createSong(String title, String artist, String email, MultipartFile file123) throws Exception;

    String deleteSong(int song_id, String email) throws UserException, SongException;
    Song getSong(int id, String email) throws  UserException,SongException;
    Song getSong(String title, String email) throws  UserException,SongException;
    List<Song> getAllSongs(String email) throws UserException, SongException;
}
