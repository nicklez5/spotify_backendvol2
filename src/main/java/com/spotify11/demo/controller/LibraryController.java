package com.spotify11.demo.controller;


import com.spotify11.demo.dtos.TrackDto;
import com.spotify11.demo.entity.Library;

import com.spotify11.demo.exception.LibraryException;
import com.spotify11.demo.exception.SongException;
import com.spotify11.demo.exception.UserException;
import com.spotify11.demo.security.CustomUserPrincipal;
import com.spotify11.demo.services.LibraryService;
import com.spotify11.demo.services.SongService;

import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/library")
public class LibraryController {

    private final LibraryService libraryService;
    private final SongService songService;
    public LibraryController(LibraryService libraryService, SongService songService) {
        this.libraryService = libraryService;
        this.songService = songService;
    }

    @PostMapping("/addSong/{songId}")
    public ResponseEntity<Void> add2(@AuthenticationPrincipal CustomUserPrincipal me,
                                    @PathVariable Integer songId) {
        libraryService.addExistingSong2(me.getId(), songId);
        return ResponseEntity.noContent().build();
    }  


    @PostMapping("/songs/{songId}")
    public ResponseEntity<Void> add(@AuthenticationPrincipal CustomUserPrincipal me,
                                    @PathVariable Integer songId) {
        libraryService.addExistingSong(me.getId(), songId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/songs/{songId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal CustomUserPrincipal me,
                                        @PathVariable Integer songId) {
        libraryService.removeSong(me.getId(), songId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/songs")
    public List<TrackDto> list(@AuthenticationPrincipal CustomUserPrincipal me) {
        return libraryService.list(me.getId());
    }
    @PostMapping("/clear")
    public ResponseEntity<Void> clear(@AuthenticationPrincipal CustomUserPrincipal me){
        libraryService.clear(me.getId());
        return ResponseEntity.noContent().build();
    }
}

