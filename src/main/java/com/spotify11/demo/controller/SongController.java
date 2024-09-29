package com.spotify11.demo.controller;

import com.spotify11.demo.entity.Song;
import com.spotify11.demo.exception.MentionedFileNotFoundException;
import com.spotify11.demo.repo.SongRepo;
import com.spotify11.demo.repo.UserRepository;

import com.spotify11.demo.exception.SongException;
import com.spotify11.demo.exception.UserException;
import com.spotify11.demo.response.UploadFileResponse;
import com.spotify11.demo.services.SongService;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import java.util.List;

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
    public Song getSong(@PathVariable("id") int id, @RequestParam("email") String email) throws UserException, SongException {
        Song str1 = songService.getSong(id,email);

        return str1;
    }

    @Transactional
    @PostMapping("/upload")
    public ResponseEntity<Song> upload(@RequestParam("email") String email, @RequestParam("title") String title, @RequestParam("artist") String artist, @RequestParam("file") MultipartFile file123, HttpServletResponse response) throws Exception {
        Song str1 = songService.createSong(title,artist,email,file123);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", file123.getOriginalFilename());
        return ResponseEntity.ok().headers(headers).body(str1);

    }
    @Transactional
    @PutMapping("/editSong/{song_id}")
    public ResponseEntity<Song> editSong(@PathVariable("song_id") int song_id, @RequestParam("title") String title, @RequestParam("artist") String artist, @RequestParam("email") String email) throws UserException, SongException, IOException {
        Song song1 = songService.updateSong(title,artist,song_id,email);

        return ResponseEntity.ok().body(song1);

    }
    @Transactional
    @DeleteMapping("/deleteSong/{song_id}")
    public ResponseEntity<String> delete_song(@PathVariable("song_id") int song_id, @RequestParam("email") String email) throws  UserException, SongException {
        String song1 = songService.deleteSong(song_id,email);
        return ResponseEntity.ok().body(song1);

    }
    @Transactional
    @GetMapping("/all")
    public ResponseEntity<List<Song>> getAllSongs(@RequestParam("email") String email ) throws UserException, SongException {
        return ResponseEntity.ok(songService.getAllSongs(email));

    }

    @Transactional
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable(value = "fileName") String filename, HttpServletRequest request) throws IOException, MentionedFileNotFoundException {

        Resource resource = songService.loadFileAsResource(filename);
        String contentType = null;
        contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        if (contentType == null) {
            logger.info("Could not determine file type.");
        }
        assert contentType != null;
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);

    }

}
