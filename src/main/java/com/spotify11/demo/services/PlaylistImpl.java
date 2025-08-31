package com.spotify11.demo.services;

import com.spotify11.demo.dtos.CreatePlaylistDto;
import com.spotify11.demo.dtos.PlaylistDto;
import com.spotify11.demo.entity.Playlist;
import com.spotify11.demo.entity.Song;
import com.spotify11.demo.entity.User;

import com.spotify11.demo.exception.SongException;
import com.spotify11.demo.exception.UserException;
import com.spotify11.demo.repo.LibraryRepo;
import com.spotify11.demo.repo.PlaylistRepo;
import com.spotify11.demo.repo.SongRepo;

import com.spotify11.demo.repo.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin
@Service
public class PlaylistImpl implements PlaylistService {



    private final UserRepository userRepo;
    private final PlaylistRepo playlistRepo;
    private final SongRepo songRepo;
    private final S3Client s3;
    private final LibraryRepo libraryRepo;
    @Value("${app.s3.bucket}") String bucket;
    // private final S3Client s3;
    public PlaylistImpl(UserRepository userRepo, PlaylistRepo playlistRepo, SongRepo songRepo, LibraryRepo libraryRepo, S3Client s3) {
        this.userRepo = userRepo;
        this.playlistRepo = playlistRepo;
        this.songRepo = songRepo;
        this.libraryRepo = libraryRepo;
        this.s3 = s3;
    }

    @Override
    public String getPlaylistName(String email, int id) throws UserException {
        if(userRepo.findByEmail(email).isPresent()){
            User user = userRepo.findByEmail(email).get();
            Playlist p = playlistRepo.findByIdAndOwnerId(id,user.getId()).orElseThrow(() -> new UserException("Playlist not found"));
            return p.getPlaylistName();
        }else{
            throw new UserException("you are not present my lord:" + email);
        }
    }



    @Transactional
    @Override
    public void RemovePlaylist(int userId, int playlistId) throws UserException{
        Playlist p = playlistRepo.findByIdAndOwnerId(playlistId,userId).orElseThrow(() -> new UserException("Playlist not found"));

        if (!Objects.equals(p.getOwner().getId(), userId))
            throw new AccessDeniedException("Not the owner");

        // Either orphanRemoval path…
        p.getOwner().removePlaylist(p);   // removes from list + sets owner=null
        // userRepo.save(p.getOwner());   // not required in same txn

        // …or delete directly (also fine):
        // playlistRepo.delete(p);
    }
    @Transactional(readOnly = true)
    public List<PlaylistDto> listUserPlaylists(int userId) {
        return playlistRepo.findByOwnerIdOrderByIdAsc(userId)
                .stream().map(PlaylistDto::from).toList();
    }
    @Transactional
    @Override
    public PlaylistDto addPlaylist(Integer userId, CreatePlaylistDto dto){

        User user = userRepo.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Playlist p = new Playlist();
        p.setPlaylistName(dto.name());
        p.setOwner(user);
        user.getPlaylists().add(p);
        Playlist saved = playlistRepo.save(p);


        return PlaylistDto.from(saved);
    }


    @Override
    public PlaylistDto updateCover(Integer ownerId, Integer playlistId, MultipartFile file) throws RuntimeException, IOException{
        Playlist p = playlistRepo.findByIdAndOwnerId(playlistId, ownerId)
        .orElseThrow(() -> new RuntimeException("Playlist not found"));

        if (file.isEmpty()) throw new RuntimeException("Empty file");
        String ct = Optional.ofNullable(file.getContentType()).orElse("");
        if (!(ct.equals("image/png") || ct.equals("image/jpeg"))) {
        throw new RuntimeException("Only PNG/JPEG allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) throw new RuntimeException("Max 5MB");

        // delete old cover if present
        if (p.getCoverKey() != null) {
        s3.deleteObject(b -> b.bucket(bucket).key(p.getCoverKey()));
        }

        String ext = ct.equals("image/png") ? ".png" : ".jpg";
        String key = "playlist-covers/%d/%s%s".formatted(p.getId(), UUID.randomUUID(), ext);

        s3.putObject(o -> o.bucket(bucket).key(key).contentType(ct),
                    RequestBody.fromBytes(file.getBytes()));

        // if you want public read, either set a CloudFront URL or use the S3 website/virtual-host URL:
        String url = "https://%s.s3.amazonaws.com/%s".formatted(bucket, key);

        p.setCoverKey(key);
        p.setCoverUrl(url);
        playlistRepo.save(p);
        return PlaylistDto.from(p);
    }

    @Transactional
    @Override
    public PlaylistDto rename(int userId, int playlistId, String newName) throws UserException{
        Playlist p = playlistRepo.findByIdAndOwnerId(playlistId,userId).orElseThrow(() -> new UserException("Playlist not found"));
        if (!Objects.equals(p.getOwner().getId(), userId))
            throw new AccessDeniedException("Not the owner");
        p.setPlaylistName(newName);
        return PlaylistDto.from(p);
    }

    @Transactional
    @Override
    public PlaylistDto clearPlaylist(String email, int id) throws UserException {
            if(userRepo.findByEmail(email).isPresent()) {
                User user = userRepo.findByEmail(email).get();
                Playlist p = playlistRepo.findByIdAndOwnerId(id,user.getId()).orElseThrow(() -> new UserException("Playlist not found"));
                p.getTracks().clear();
                playlistRepo.save(p);


                return PlaylistDto.from(p);

            }else{
                throw new UserException("User is not present");
            }


    }


    


}
