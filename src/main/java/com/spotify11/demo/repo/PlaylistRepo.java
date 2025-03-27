package com.spotify11.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spotify11.demo.entity.Playlist;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepo extends JpaRepository<Playlist, Integer> {
    Playlist findByPlaylistName(String playlist_name);
}
