package com.spotify11.demo.controller;

import com.spotify11.demo.dtos.CreateSongDto;
import com.spotify11.demo.dtos.SongDto;
import com.spotify11.demo.dtos.TrackDto;
import com.spotify11.demo.entity.Song;
import com.spotify11.demo.exception.MentionedFileNotFoundException;
import com.spotify11.demo.repo.SongRepo;
import com.spotify11.demo.repo.UserRepository;

import com.spotify11.demo.exception.SongException;
import com.spotify11.demo.exception.UserException;
import com.spotify11.demo.response.UploadFileResponse;
import com.spotify11.demo.security.CustomUserPrincipal;
import com.spotify11.demo.services.SongService;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import java.util.List;
import java.util.Map;

import com.spotify11.demo.services.FileSystemStorageService;



@CrossOrigin
@RestController
@RequestMapping("/songs")
public class SongController {

    private static final Logger logger = LoggerFactory.getLogger(SongController.class);

    @Autowired
    private SongService songService;


    @Autowired
    private FileSystemStorageService storageService;

    @Autowired
    private SongRepo songRepo;

    @Autowired
    private UserRepository userRepo;


    @Transactional
    @GetMapping("/info/{id}")
    public Song getSong(@PathVariable("id") int id, @AuthenticationPrincipal CustomUserPrincipal me) throws UserException, SongException {
        Song str1 = songService.getSong(id,me.getEmail());

        return str1;
    }

    
     @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserPrincipal me,
                                        @PathVariable Integer id) {
        songService.deleteSong(me.getId(), id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping
    public ResponseEntity<List<TrackDto>> listSongs(@AuthenticationPrincipal CustomUserPrincipal me) throws UserException{
        var xyz = songService.getAllSongsFromUser(me.getEmail());
        return ResponseEntity.ok().body(xyz);
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<TrackDto>> listSongsAll(@AuthenticationPrincipal CustomUserPrincipal me) throws UserException{
        var xyz = songService.getAllSongs(me.getEmail());
        return ResponseEntity.ok().body(xyz);
    }
    
    
    @PostMapping
    public ResponseEntity<SongDto> create(@AuthenticationPrincipal CustomUserPrincipal me,
                            @RequestBody CreateSongDto dto) throws UserException {
        var dto1 = songService.create(me.getId(), dto);
        return ResponseEntity.ok().body(dto1);
    }

    @PostMapping("/{id}/upload-url")
    public Map<String, String> presign(@AuthenticationPrincipal CustomUserPrincipal me,
                                        @PathVariable Integer id,
                                        @RequestParam String contentType) {
        return songService.presignUpload(me.getId(), id, contentType);
    }
    
    @PostMapping("/{id}/finalize")
    public SongDto finalize(@AuthenticationPrincipal CustomUserPrincipal me,
                            @PathVariable Integer id,
                            @RequestParam Long sizeBytes,
                            @RequestParam(required = false) Integer durationSec) {
        return songService.finalizeUpload(me.getId(), id, sizeBytes, durationSec);
    }
    @GetMapping("/{id}/stream-url")
    public Map<String, String> streamUrl(@AuthenticationPrincipal CustomUserPrincipal me,
                                        @PathVariable Integer id) {
        return Map.of("url", songService.presignedGet( id));
    }
    public String getMethodName(@RequestParam String param) {
        return new String();
    }
    

}
