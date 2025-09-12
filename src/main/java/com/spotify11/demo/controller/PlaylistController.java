package com.spotify11.demo.controller;

import com.spotify11.demo.dtos.PlaylistDetailDto;
import com.spotify11.demo.dtos.PlaylistDto;
import com.spotify11.demo.dtos.TrackDto;
import com.spotify11.demo.dtos.UpdateVisibilityDto;
import com.spotify11.demo.entity.Playlist;
import com.spotify11.demo.entity.Song;

import com.spotify11.demo.entity.User;
import com.spotify11.demo.enums.Visibility;
import com.spotify11.demo.exception.PlaylistException;

import com.spotify11.demo.exception.UserException;

import com.spotify11.demo.repo.PlaylistRepo;
import com.spotify11.demo.repo.UserRepository;
import com.spotify11.demo.security.CustomUserPrincipal;
import com.spotify11.demo.services.PlaylistService;
import com.spotify11.demo.services.PlaylistTrackService;
import com.spotify11.demo.services.SongService;

import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/playlist")
public class PlaylistController {

    private final PlaylistService playlistService;
    private PlaylistRepo playlistRepo;
    private final SongService songService;
    private final UserRepository userRepo;
    private final PlaylistTrackService playlistTrackService;
    public PlaylistController(PlaylistService playlistService, SongService songService, UserRepository userRepo, PlaylistRepo playlistRepo, PlaylistTrackService playlistTrackService) {
        this.playlistService = playlistService;
        this.userRepo = userRepo;
        this.playlistRepo = playlistRepo;
        this.playlistTrackService = playlistTrackService;
        this.songService = songService;
    }

    @Transactional
    @PatchMapping("/{id}/makeprivate")
    public ResponseEntity<PlaylistDto> setPrivate(@AuthenticationPrincipal CustomUserPrincipal me, @PathVariable("id") int id, @RequestBody UpdateVisibilityDto dto){
        var dto2 = playlistService.changeVisibility(me.getId(), id, dto.visibility() );
        
        return ResponseEntity.ok().body(dto2);
    }
    @Transactional
    @GetMapping("/{id}/user/{user_id}/info")
    public ResponseEntity<PlaylistDto> getPlaylist(@PathVariable("user_id") int user_id, @PathVariable("id") int id){
        User user = userRepo.findById(user_id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Playlist p = user.getPlaylists().stream()
            .filter(pl -> pl.getId() == (id))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
        return ResponseEntity.ok(PlaylistDto.from(p));
    }
    // ADD SONG

    @Transactional
    @PostMapping("/{id}/addSong/{song_id}")
    public ResponseEntity<PlaylistDetailDto> addSongForPlaylist(@AuthenticationPrincipal CustomUserPrincipal me,@PathVariable("id") int id,@PathVariable("song_id") int song_id) throws Exception {
        var dto = playlistTrackService.addSongToPlaylist(me.getId(), id, song_id);
            return ResponseEntity.ok().body(dto);

    }

    @PostMapping(value="/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public PlaylistDto uploadCover(
        @AuthenticationPrincipal CustomUserPrincipal me,
        @PathVariable Integer id,
        @RequestPart("file") MultipartFile file) throws IOException {
        return playlistService.updateCover(me.getId(), id, file);
    }

    
    @GetMapping("/{id}/user/{user_id}")
    public List<TrackDto> tracks(@PathVariable Integer user_id,
                             @PathVariable Integer id) {
    return songService.listTracksForPlaylist(user_id, id);
    }
    // @PutMapping("/{id}/cover")
    // public ResponseEntity<PlaylistDto> uploadCover(
    //     @AuthenticationPrincipal CustomUserPrincipal me,
    //     @PathVariable Integer id,
    //     @RequestParam("file") MultipartFile file) throws IOException{
    //         //var dto = playlistService.update;
    //     }
    
    @DeleteMapping("/{id}/removeSong/{song_id}")
    public ResponseEntity<PlaylistDetailDto> removeSongFromPlaylist(@AuthenticationPrincipal CustomUserPrincipal me,@PathVariable("id") int id, @PathVariable("song_id") int song_id) throws Exception {
        try{
            var dto = playlistTrackService.removeSongFromPlaylist(me.getId(), id, song_id);
            return ResponseEntity.ok().body(dto);
        } catch (Exception e) {
            throw new Exception("Song name: " + song_id + "could not be found");
        }

    }

    @Transactional
    @GetMapping(value = "/{id}/user/{user_id}/detail")
    public PlaylistDetailDto getSongs(@PathVariable("id") int id,@PathVariable("user_id") int user_id) throws Exception {
        try{
            Integer viewerId = null;
            viewerId = userRepo.findById(user_id).map(User::getId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Viewer email not found"));
            return playlistTrackService.viewPlaylist(viewerId, false, id);
        } catch (Exception e) {
            throw new Exception("Could not find user with id: " + user_id);
        }

    }

    @GetMapping("/users/{id}/playlists/with-songs")
    public List<PlaylistDetailDto> userPlaylistsWithSongs(@PathVariable int id,
      @AuthenticationPrincipal CustomUserPrincipal me) {
    Integer viewerId = (me.getEmail() == null || me.getEmail().isBlank())
        ? null
        : userRepo.findByEmail(me.getEmail()).map(User::getId).orElse(null);
    return playlistTrackService.listUserPlaylistsWithSongs(viewerId, false, id);
  }
    @GetMapping("/search")
    //RENAME
    public List<PlaylistDetailDto> getPlaylists(@RequestParam(name = "query", required = false) String searchTerm){
        var dto = playlistTrackService.listPlaylistsWithSongs(searchTerm);
        return dto;
    }
    // CLEAR

    @Transactional
    @DeleteMapping("/{id}/clear")
    public ResponseEntity<PlaylistDto> clearPlaylist(@PathVariable("id") int id,@AuthenticationPrincipal CustomUserPrincipal me) throws UserException {
        var dto = playlistService.clearPlaylist(me.getUsername(),id);
        return ResponseEntity.ok().body(dto);
    }

    // GET A PLAYLIST

    @Transactional
    @GetMapping("/{id}/getPlaylistName")
    public ResponseEntity<String> getPlaylistName(@PathVariable("id") int id,@AuthenticationPrincipal CustomUserPrincipal me) throws UserException, PlaylistException {
        String str1 = playlistService.getPlaylistName(me.getEmail(), id);
        return ResponseEntity.ok(str1);
    }

     @Transactional
    @PostMapping("/{id}/setPlaylistName")
    public ResponseEntity<PlaylistDto> setPlaylistName(@PathVariable("id") int id,@AuthenticationPrincipal CustomUserPrincipal me, @RequestBody Map<String, String> body) throws UserException, PlaylistException {
        var dto = playlistService.rename(me.getId(), id, body.get("name"));
        return ResponseEntity.ok().body(dto);
    }

    @Transactional
    @GetMapping("/{id}/getPlaylistDescription")
    public ResponseEntity<String> getPlaylistDescription(@PathVariable("id") int id, @AuthenticationPrincipal CustomUserPrincipal me) throws UserException, PlaylistException{
        String str2 = playlistService.getPlaylistDescription(me.getEmail(), id);
        return ResponseEntity.ok(str2);
    }
    @Transactional
    @PostMapping("/{id}/setPlaylistDescription")
    public ResponseEntity<PlaylistDto> setPlaylistDescription(@PathVariable("id") int id, @AuthenticationPrincipal CustomUserPrincipal me, @RequestBody Map<String, String> body) throws UserException, PlaylistException{
        var dto = playlistService.renameDescription(me.getId(), id, body.get("description"));
        return ResponseEntity.ok().body(dto);
    }




}
