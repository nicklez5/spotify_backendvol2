package com.spotify11.demo.services;

import com.spotify11.demo.entity.Playlist;
import com.spotify11.demo.entity.Song;
import com.spotify11.demo.entity.User;

import com.spotify11.demo.exception.SongException;
import com.spotify11.demo.exception.UserException;
import com.spotify11.demo.repo.LibraryRepo;
import com.spotify11.demo.repo.PlaylistRepo;
import com.spotify11.demo.repo.SongRepo;

import com.spotify11.demo.repo.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Iterator;
import java.util.List;

@CrossOrigin
@Service
public class PlaylistImpl implements PlaylistService {



    private final UserRepository userRepo;
    private final PlaylistRepo playlistRepo;
    private final SongRepo songRepo;
    private final LibraryRepo libraryRepo;

    public PlaylistImpl(UserRepository userRepo, PlaylistRepo playlistRepo, SongRepo songRepo, LibraryRepo libraryRepo) {
        this.userRepo = userRepo;
        this.playlistRepo = playlistRepo;
        this.songRepo = songRepo;
        this.libraryRepo = libraryRepo;
    }

    @Override
    public String getPlaylistName(String email) throws UserException {
        if(userRepo.findByEmail(email).isPresent()){
            User user = userRepo.findByEmail(email).get();
            Playlist playlist1 = user.getPlaylist();
            return playlist1.getPlaylistName();
        }else{
            throw new UserException("you are not present my lord:" + email);
        }
    }

    @Transactional
    @Override
    public Playlist addSong(int song_id, String email) throws Exception {

        if (userRepo.findByEmail(email).isPresent()) {
            User user = userRepo.findByEmail(email).get();
            Playlist playlist1 = user.getPlaylist();
            Song song1 = songRepo.findById(song_id).get();
            playlist1.getSongs().add(song1);
            playlistRepo.save(playlist1);
            userRepo.save(user);
            return playlist1;


        }else{
            throw new Exception("Homie wasnt here");
        }



    }







    @Transactional
    @Override
    public String renamePlaylist(String email, String playlist_name) throws UserException {
            if(userRepo.findByEmail(email).isPresent()) {
                User user = userRepo.findByEmail(email).get();
                Playlist playlist1 = user.getPlaylist();
                playlist1.setPlaylistName(playlist_name);
                playlistRepo.save(playlist1);
                userRepo.save(user);

                return "Your playlist has been renamed to " + playlist_name;

            }

        return null;
    }

    @Transactional
    @Override
    public Playlist clearPlaylist(String email) throws UserException {
            if(userRepo.findByEmail(email).isPresent()) {
                User user = userRepo.findByEmail(email).get();
                Playlist ply1 = user.getPlaylist();
                user.getPlaylist().getSongs().clear();
                userRepo.save(user);


                return ply1;

            }else{
                throw new UserException("User is not present");
            }


    }

    @Override
    public List<Song> getSongs(String email) throws UserException {
        if(userRepo.findByEmail(email).isPresent()){
            User user = userRepo.findByEmail(email).get();
            Playlist playlist1 = user.getPlaylist();
            return playlist1.getSongs();
        }else{
            throw new UserException("User does not exist");
        }


    }
    @Transactional
    @Override
    public Playlist removeSong(int song_id, String email) throws SongException, UserException {
        if(userRepo.findByEmail(email).isPresent()) {
            User user = userRepo.findByEmail(email).get();
            Playlist playlist1 = user.getPlaylist();
            Song song1 = songRepo.findById(song_id).orElse(null);
            playlist1.getSongs().remove(song1);
            playlistRepo.save(playlist1);
            userRepo.save(user);
            return playlist1;

        }else{
            throw new UserException("User is not present");
        }

    }


}
