package com.spotify11.demo.services;

import com.spotify11.demo.entity.Library;
import com.spotify11.demo.entity.Song;
import com.spotify11.demo.exception.*;

import java.util.List;

public interface LibraryService {

    Library addSong(Song song1, String email) throws SongException, UserException;
    Library deleteSong(Song song1, String email) throws SongException, UserException;
    Library getLibrary(String email) throws LibraryException;
    Library clearLibrary(String email) throws LibraryException;


}
