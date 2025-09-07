package com.spotify11.demo.services;

import com.spotify11.demo.dtos.CreateSongDto;
import com.spotify11.demo.dtos.SongDto;
import com.spotify11.demo.dtos.TrackDto;
import com.spotify11.demo.entity.Playlist;
import com.spotify11.demo.entity.Song;
import com.spotify11.demo.entity.UploadStatus;
import com.spotify11.demo.entity.User;
import com.spotify11.demo.exception.FileStorageException;
import com.spotify11.demo.exception.MentionedFileNotFoundException;
import com.spotify11.demo.exception.SongException;

import com.spotify11.demo.exception.UserException;
import com.spotify11.demo.property.FileStorageProperties;
import com.spotify11.demo.repo.PlaylistRepo;
import com.spotify11.demo.repo.SongRepo;
import com.spotify11.demo.repo.UserRepository;
import com.spotify11.demo.utilites.Functions;
import jakarta.transaction.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SongImpl implements SongService {

    private static final Logger log = LoggerFactory.getLogger(SongImpl.class);

    private final UserRepository userRepo;
    private final S3Client s3;
    private final PlaylistRepo playlistRepo;
    private final SongRepo songRepo;
    private final S3Presigner presigner;
    public Functions functions;
    private final Path fileStorageLocation;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.public-urls:false}")
    private boolean usePublicUrls;
    
    @Value("${app.cdn.domain:}")
    private String cdnDomain;

    public SongImpl(UserRepository userRepo, PlaylistRepo playlistRepo, SongRepo songRepo,FileStorageProperties fileStorageProperties, S3Client s3, S3Presigner presigner) throws IOException {
        this.songRepo = songRepo;
        this.userRepo = userRepo;
        this.s3 = s3;
        this.presigner = presigner;
        this.playlistRepo = playlistRepo;
        this.functions = new Functions();
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try{
            Files.createDirectories(fileStorageLocation);
        }catch(Exception e){
            throw new FileStorageException("Unable to create directory for storing files", e);
        }
        //noinspection InstantiationOfUtilityClass


    }

    @Override
    @Transactional
    public SongDto create(Integer ownerId, CreateSongDto dto) throws UserException{
        Song s = new Song();
        s.setOwnerId(ownerId);
        s.setTitle(dto.title());
        s.setArtist(dto.artist());
        s.setStatus(UploadStatus.PENDING);
        s = songRepo.save(s);
        
        var u = userRepo.findByIdWithLibrary(ownerId)
            .orElseThrow(() -> new UserException("User not found"));
        u.getLibrary().addSong(s);

        userRepo.save(u);
        return SongDto.from(s,null);
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
            url = presignedGet(s.getId());
        }
        }

        return TrackDto.from(s, url);
    }

    public SongDto finalizeUpload(Integer ownerId, Integer songId, Long sizeBytes, Integer durationSec) {
        Song s = songRepo.findByIdAndOwnerId(songId, ownerId)
            .orElseThrow(() -> new RuntimeException("Song not found"));
        if (s.getS3Key() == null) throw new RuntimeException("No upload in progress");

        // optional: check object exists in S3 (HeadObject)
        try {
        s3.headObject(b -> b.bucket(bucket).key(s.getS3Key()));
        } catch (Exception ex) {
        s.setStatus(UploadStatus.FAILED);
        songRepo.save(s);
        throw new RuntimeException("Object not found in S3; upload failed");
        }
        if (durationSec != null) s.setDurationSec(durationSec);
        s.setSizeBytes(sizeBytes);
        s.setStatus(UploadStatus.READY);
        songRepo.save(s);

        // If public bucket/CloudFront, build a public URL; otherwise return null here and presign GET when needed
        String url = "https://" + bucket + ".s3.amazonaws.com/" + s.getS3Key();
        return SongDto.from(s, url);
    }
    @Override
    public Map<String, String> presignUpload(Integer ownerId, Integer songId, String contentType){
        Song s = songRepo.findByIdAndOwnerId(songId, ownerId)
        .orElseThrow(() -> new RuntimeException("Song not found"));

        if (contentType == null || !contentType.startsWith("audio/")) {
        throw new RuntimeException("Only audio uploads allowed");
        }

        String key = "users/%d/songs/%d/%s".formatted(ownerId, songId, java.util.UUID.randomUUID());

        var put = software.amazon.awssdk.services.s3.model.PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build();

        var presigned = presigner.presignPutObject(p -> p
            .putObjectRequest(put)
            .signatureDuration(java.time.Duration.ofMinutes(15)));

        // Remember the key & mark uploading
        s.setS3Key(key);
        s.setContentType(contentType);
        s.setStatus(UploadStatus.UPLOADING);
        songRepo.save(s);

        return Map.of("url", presigned.url().toString(), "key", key);
    }

    @Transactional
    public String presignedGet(Integer songId) {
        Song s = songRepo.findById(songId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));
        if (s.getS3Key() == null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Song has no storage key");

        GetObjectRequest getReq = GetObjectRequest.builder()
            .bucket(bucket).key(s.getS3Key()).build();

        return presigner.presignGetObject(b -> b
                .getObjectRequest(getReq)
                .signatureDuration(Duration.ofHours(12)))
            .url().toString();
    }






   
    @Transactional
    public void deleteSong(Integer ownerId, Integer songId) {
        playlistRepo.unlinkSongFromAllPlaylists(songId);
        Song s = songRepo.findByIdAndOwnerId(songId, ownerId)
            .orElseThrow(() -> new RuntimeException("Song not found"));

        String key = s.getS3Key();

        // 1) Delete the object in S3 (safe/idempotent)
        safeDeleteS3(key);

        // 2) Remove DB relationships if you have many-to-many links (optional)
        // playlistSongRepo.deleteBySongId(songId); // example if you have a join table

        // 3) Delete the row
        songRepo.delete(s);
    }
    private void safeDeleteS3(String key) {
        if (key == null || key.isBlank()) return;
        try {
        s3.deleteObject(b -> b.bucket(bucket).key(key));
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
        // Ignore "NoSuchKey" (already gone); rethrow others
        String code = e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "";
        if (!"NoSuchKey".equals(code)) {
            throw e;
        }
        }
    }

    

    public Song getSong(int song_id, String email) throws SongException {
        if (userRepo.findByEmail(email).isPresent()) {
            User user = userRepo.findByEmail(email).get();

            Set<Song> xyz = user.getLibrary().getSongs();
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
            Set<Song> xyz = user.getLibrary().getSongs();
            for (Song song : xyz) {
                if (song.getTitle().equals(title)) {
                    return this.songRepo.findByTitle(title).get();
                }
            }
        }

            throw new SongException("Song title: " + title + " could not be found");

    }


    public List<TrackDto> getAllSongs(String email) throws UserException {
        var user = userRepo.findByEmailWithLibrary(email)
            .orElseThrow(() -> new UserException("User not found"));
        List<Song> songs = songRepo.findByLibraryId(user.getLibrary().getId());
        return songs.stream().map(s -> toTrackDto(s, user.getId())).toList();

    }
    @Override
    public List<TrackDto> listTracksForPlaylist(Integer ownerId, Integer playlistId) {
        List<Song> songs = songRepo.findByPlaylistAndOwner(playlistId, ownerId);
        return songs.stream().map(s -> toTrackDto(s, ownerId)).toList();
    }
}
