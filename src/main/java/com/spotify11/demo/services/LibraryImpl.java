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
import jakarta.transaction.Transactional;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

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

     public void addExistingSong(Integer ownerId, Integer songId) {
        var lib = getOrCreateLibrary(ownerId);
        var song = songRepo.findByIdAndOwnerId(songId, ownerId)
            .orElseThrow(() -> new RuntimeException("Song not found or not yours"));
        // attach
        lib.addSong(song);       // keeps both sides consistent
        // because of cascade on Library.songs, saving lib is enough
        libraryRepo.save(lib);
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
    public void removeSong(Integer ownerId, Integer songId) {
        var lib = libraryRepo.findByOwnerId(ownerId)
            .orElseThrow(() -> new RuntimeException("Library not found"));
        var song = songRepo.findByIdAndOwnerId(songId, ownerId)
            .orElseThrow(() -> new RuntimeException("Song not found"));
        lib.removeSong(song);
        libraryRepo.save(lib);
        // If orphanRemoval=true -> the Song row is deleted automatically.
        // If orphanRemoval=false -> the Song row remains with library_id null.
    }

    public List<TrackDto> list(Integer ownerId) {
        List<Song> songs = songRepo.findByLibraryOwnerId(ownerId);
        return songs.stream()
                .map(s -> toTrackDto(s, ownerId))
                .toList();
    }
    
    public TrackDto toTrackDto(Song s, Integer ownerId) {
        String url = null;

        if (s.getStatus() == UploadStatus.READY && s.getS3Key() != null) {
        if (usePublicUrls) {
            // public (or CloudFront) path
            String base = (cdnDomain != null && !cdnDomain.isBlank())
                ? "https://" + cdnDomain + "/"
                : "https://" + bucket + ".s3.amazonaws.com/";
            url = base + s.getS3Key();
        } else {
            // private bucket → presign via your new helper
            url = presignedGet(ownerId, s.getId());
        }
        }

        return TrackDto.from(s, url);
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

    
}
