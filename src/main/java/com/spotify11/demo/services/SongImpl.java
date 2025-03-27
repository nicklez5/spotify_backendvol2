package com.spotify11.demo.services;

import com.spotify11.demo.entity.Song;
import com.spotify11.demo.entity.User;
import com.spotify11.demo.exception.FileStorageException;
import com.spotify11.demo.exception.MentionedFileNotFoundException;
import com.spotify11.demo.exception.SongException;

import com.spotify11.demo.exception.UserException;
import com.spotify11.demo.property.FileStorageProperties;
import com.spotify11.demo.repo.SongRepo;
import com.spotify11.demo.repo.UserRepository;
import com.spotify11.demo.utilites.Functions;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class SongImpl implements SongService {

    private static final Logger log = LoggerFactory.getLogger(SongImpl.class);

    private final UserRepository userRepo;



    private final SongRepo songRepo;

    public Functions functions;
    private final Path fileStorageLocation;


    public SongImpl(UserRepository userRepo, SongRepo songRepo,FileStorageProperties fileStorageProperties) throws IOException {
        this.songRepo = songRepo;
        this.userRepo = userRepo;
        this.functions = new Functions();
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try{
            Files.createDirectories(fileStorageLocation);
        }catch(Exception e){
            throw new FileStorageException("Unable to create directory for storing files", e);
        }
        //noinspection InstantiationOfUtilityClass


    }

    @Transactional
    @Override
    public Song createSong(String title, String artist, String email, MultipartFile file123) throws Exception {
        if (userRepo.findByEmail(email).isPresent()) {



            User user = userRepo.findByEmail(email).get();
            String fileName = file123.getOriginalFilename();
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file123.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/songs/download/" + fileName)
                    .build()
                    .toUriString();


            Song song123 = new Song(title,artist,fileDownloadUri);
            user.getLibrary().addSong(song123, email);
            songRepo.save(song123);

            return song123;

        }else{
            throw new UserException("User not found");
        }

    }



    @Transactional
    @Override
    public Song updateSong(String title, String artist, int song_id, String email) throws UserException {

            User user = userRepo.findByEmail(email).get();
            List<Song> xyz = user.getLibrary().getSongs();
            for (Song song : xyz) {
                if (song.getId() == song_id) {
                    song.setArtist(artist);
                    song.setTitle(title);
                    songRepo.save(song);
                    return song;
                }
            }


        return null;
    }



    @Transactional
    @Override
    public Resource loadFileAsResource(String fileName) throws MentionedFileNotFoundException, FileNotFoundException {
        try{
            Path filePath = Path.of(Functions.basePath + File.separator + fileName);
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()){
                return resource;
            }else{
                throw new FileNotFoundException("File not found " + fileName);
            }
        }catch(MalformedURLException | FileNotFoundException ex){
            throw new FileNotFoundException("File not found" + fileName);
        }
    }

    @Override
    public File multipartFile(MultipartFile file, String fileName) throws IOException {
        Path filePath = Path.of(Functions.basePath + File.separator + fileName);
        File convFile = new File(filePath.toUri());
        file.transferTo(convFile);
        return convFile;
    }



    @Transactional
    @Override
    public String deleteSong(int song_id, String email) throws SongException {

            if(userRepo.findByEmail(email).isPresent()){
                User user = userRepo.findByEmail(email).get();
                List<Song> xyz = user.getLibrary().getSongs();
                for (Song song : xyz) {
                    if (song.getId() == song_id) {
                        user.getLibrary().removeSong(song);
                        songRepo.delete(song);

                        return String.valueOf(song_id);
                    }
                }
            }else{
                throw new SongException("Song cant be foundexist");
            }
        return null;


    }


    public Song getSong(int song_id, String email) throws SongException {
        if (userRepo.findByEmail(email).isPresent()) {
            User user = userRepo.findByEmail(email).get();

            List<Song> xyz = user.getLibrary().getSongs();
            for (Song song : xyz) {
                if (song.getId() == song_id) {
                        return song;
                }
            }
            throw new SongException("Song id:" + song_id + " has not been found");

        }
        return null;
    }

    public Song getSong(String title, String email) throws  SongException {
        if(userRepo.findByEmail(email).isPresent()) {
            User user = userRepo.findByEmail(email).get();
            List<Song> xyz = user.getLibrary().getSongs();
            for (Song song : xyz) {
                if (song.getTitle().equals(title)) {
                    return this.songRepo.findByTitle(title).get();
                }
            }
        }

            throw new SongException("Song title: " + title + " could not be found");

    }


    public List<Song> getAllSongs(String email) throws UserException {
        User user = userRepo.findByEmail(email).get();
        List<Song> xyz = user.getLibrary().getSongs();
        return xyz;
    }
}
