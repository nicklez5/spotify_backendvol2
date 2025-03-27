package com.spotify11.demo.controller;

import com.spotify11.demo.entity.Playlist;
import com.spotify11.demo.entity.Song;

import com.spotify11.demo.entity.User;
import com.spotify11.demo.exception.PlaylistException;

import com.spotify11.demo.exception.UserException;

import com.spotify11.demo.repo.PlaylistRepo;
import com.spotify11.demo.repo.UserRepository;
import com.spotify11.demo.services.PlaylistService;

import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/playlist")
public class PlaylistController {

    private final PlaylistService playlistService;
    private PlaylistRepo playlistRepo;
    private final UserRepository userRepo;
    public PlaylistController(PlaylistService playlistService, UserRepository userRepo, PlaylistRepo playlistRepo) {
        this.playlistService = playlistService;
        this.userRepo = userRepo;
        this.playlistRepo = playlistRepo;
    }


    @Transactional
    @GetMapping("/info")
    public Playlist getPlaylist(@RequestParam("email") String email){
        if(userRepo.findByEmail(email).isPresent()){
            User user = userRepo.findByEmail(email).get();
            return user.getPlaylist();
        }
        return null;
    }
    // ADD SONG

    @Transactional
    @PostMapping("/addSong/{song_id}")
    public ResponseEntity<Playlist> addSongForPlaylist(@PathVariable("song_id") int song_id, @RequestParam("email") String email) throws Exception {
        try{
            Playlist playlist1 = playlistService.addSong(song_id, email);
            playlistRepo.save(playlist1);
            return ResponseEntity.ok(playlist1);
        } catch (Exception e) {
            throw new Exception("Song ID: " + song_id + "could not be found");
        }

    }


    @DeleteMapping("/removeSong/{song_id}")
    public ResponseEntity<Playlist> removeSongFromPlaylist(@PathVariable("song_id") int song_id, @RequestParam("email") String email) throws Exception {
        try{
            Playlist playlist1 = playlistService.removeSong(song_id, email);
            playlistRepo.save(playlist1);
            return ResponseEntity.ok(playlist1);
        } catch (Exception e) {
            throw new Exception("Song name: " + song_id + "could not be found");
        }

    }

    @Transactional
    @GetMapping(value = "/getSongs")
    public ResponseEntity<List<Song>> getSongs(@RequestParam("email") String email) throws Exception {
        try{
            List<Song> str1 = playlistService.getSongs(email);
            return ResponseEntity.ok(str1);
        } catch (Exception e) {
            throw new Exception("Could not find user with email: " + email);
        }

    }


    //RENAME

    @Transactional
    @PostMapping("/rename")
    public ResponseEntity<String> renamePlaylist(@RequestParam("email") String email, @RequestParam("playlist_name") String playlist_name) throws UserException, PlaylistException {
        String str3 = playlistService.renamePlaylist(email,playlist_name);
        return ResponseEntity.ok(str3);
    }
    // CLEAR

    @Transactional
    @DeleteMapping("/clear")
    public ResponseEntity<Playlist> clearPlaylist(@RequestParam("email") String email) throws UserException {
        Playlist str3 = playlistService.clearPlaylist(email);
        return ResponseEntity.ok(str3);
    }

    // GET A PLAYLIST

    @Transactional
    @GetMapping("/getPlaylistName")
    public ResponseEntity<String> getPlaylistName(@RequestParam("email") String email) throws UserException, PlaylistException {
        String str1 = playlistService.getPlaylistName(email);
        return ResponseEntity.ok(str1);
    }





}
