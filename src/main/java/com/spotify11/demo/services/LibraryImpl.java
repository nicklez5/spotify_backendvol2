package com.spotify11.demo.services;

import com.spotify11.demo.dtos.TrackDto;
import com.spotify11.demo.entity.Library;
import com.spotify11.demo.entity.Song;
import com.spotify11.demo.entity.UploadStatus;
import com.spotify11.demo.entity.User;


import com.spotify11.demo.exception.LibraryException;
import com.spotify11.demo.exception.SongException;
import com.spotify11.demo.exception.UserException;
import com.spotify11.demo.repo.LibraryRepo;
import com.spotify11.demo.repo.SongRepo;
import com.spotify11.demo.repo.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LibraryImpl implements LibraryService {


    private final UserRepository userRepo;

    private final LibraryRepo libraryRepo;
    private final SongRepo songRepo;
    private final S3Presigner presigner; 
    @Value("${app.s3.bucket}")            private String bucket;
    @Value("${app.s3.public-urls:false}") private boolean usePublicUrls;
    @Value("${app.cdn.domain:}")          private String cdnDomain;

    public LibraryImpl(UserRepository userRepo, LibraryRepo libraryRepo, SongRepo songRepo , S3Presigner presigner)  {
        this.userRepo = userRepo;
        this.libraryRepo = libraryRepo;
        this.songRepo = songRepo;
        this.presigner = presigner;
    }
    private Library getOrCreateLibrary(Integer ownerId) {
        return libraryRepo.findByOwnerId(ownerId).orElseGet(() -> {
        var lib = new Library();
        lib.setOwner(userRepo.getReferenceById(ownerId));
        return libraryRepo.save(lib);
        });
    }

    @Transactional
    @Override
    public void addExistingSong2(Integer ownerId, Integer songId) {
        songRepo.findById(songId).orElseThrow(() -> new RuntimeException("Song not found"));

        // get or create a library id for this user
        Integer libId = libraryRepo.findLibraryIdForUser(ownerId);
        if (libId == null) {
        // Create a Library entity in code and link it to the user (recommended)
        User u = userRepo.findById(ownerId)
            .orElseThrow(() -> new RuntimeException("User not found: " + ownerId));
        Library lib = new Library();
        lib.setOwner(u); u.setLibrary(lib);
        userRepo.save(u);                        // cascades Library
        libId = libraryRepo.findLibraryIdForUser(ownerId);
        if (libId == null) throw new IllegalStateException("Failed to create library for user " + ownerId);
        }

        libraryRepo.linkSong(libId, songId);
    }
    @Transactional
    @Override
     public void addExistingSong(Integer ownerId, Integer songId) {
         songRepo.findById(songId).orElseThrow(() -> new RuntimeException("Song not found"));

        // get or create a library id for this user
        Integer libId = libraryRepo.findLibraryIdForUser(ownerId);
        if (libId == null) {
        // Create a Library entity in code and link it to the user (recommended)
        User u = userRepo.findById(ownerId)
            .orElseThrow(() -> new RuntimeException("User not found: " + ownerId));
        Library lib = new Library();
        lib.setOwner(u); u.setLibrary(lib);
        userRepo.save(u);                        // cascades Library
        libId = libraryRepo.findLibraryIdForUser(ownerId);
        if (libId == null) throw new IllegalStateException("Failed to create library for user " + ownerId);
        }

        libraryRepo.linkSong(libId, songId); 
    }
     public Song createAndAttach(Integer ownerId, String title, String artist) {
        var lib = getOrCreateLibrary(ownerId);
        var s = new Song();
        s.setOwnerId(ownerId);
        s.setTitle(title);
        s.setArtist(artist);
        lib.addSong(s);
        libraryRepo.save(lib);
        return s;
    }
    @Transactional
    @Override
    public void removeSong(Integer ownerId, Integer songId) {
         Library lib = libraryRepo.findByOwnerIdFetchSongs(ownerId)
        .orElseThrow(() -> new RuntimeException("Library not found"));

        Song songRef = songRepo.getReferenceById(songId);

        
        lib.getSongs().removeIf(s -> s.getId().equals(songId));        // collection is initialized
       
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackDto> list(Integer ownerId) {
        
        List<Song> songs = songRepo.findSongsInLibrary(ownerId);
        return songs.stream()
                .map(s -> toTrackDto(s))
                .toList();
    }
    
    private TrackDto toTrackDto(Song s) {
        String url = null;

        if (UploadStatus.READY.equals(s.getStatus()) && s.getS3Key() != null) {
            if (usePublicUrls) {
            String base = (cdnDomain != null && !cdnDomain.isBlank())
                ? "https://" + cdnDomain + "/"
                : "https://" + bucket + ".s3.amazonaws.com/";
            String key = s.getS3Key().startsWith("/") ? s.getS3Key().substring(1) : s.getS3Key();
            url = base + key;
            } else {
            url = presignedGetByKey(s.getS3Key());   // <-- use key directly
            }
        }

        return TrackDto.from(s, url);
        }
    private String presignedGetByKey(String s3Key) {
        var getReq = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
            .bucket(bucket).key(s3Key).build();

        var pres = presigner.presignGetObject(b -> b
            .getObjectRequest(getReq)
            .signatureDuration(java.time.Duration.ofMinutes(10)));

        return pres.url().toString();
    }
     private String presignedGet(Integer ownerId, Integer songId) {
        Song s = songRepo.findByIdAndOwnerId(songId, ownerId)
            .orElseThrow(() -> new RuntimeException("Song not found"));
        if (s.getS3Key() == null) throw new RuntimeException("Song has no storage key");

        var getReq = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
            .bucket(bucket).key(s.getS3Key()).build();

        var pres = presigner.presignGetObject(b -> b
            .getObjectRequest(getReq)
            .signatureDuration(java.time.Duration.ofMinutes(10)));

        return pres.url().toString();
    }
    @Override
    public void clear(Integer ownerId){
        Library lib = libraryRepo.findByOwnerId(ownerId)
        .orElseThrow(() -> new RuntimeException("Library not found"));
        lib.getSongs().clear();
    }
    
}
