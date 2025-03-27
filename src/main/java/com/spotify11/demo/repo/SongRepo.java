package com.spotify11.demo.repo;

import com.spotify11.demo.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;


@Repository
public interface SongRepo extends JpaRepository<Song,Integer> {
    public Optional<Song>  findByTitle(String title);
    
}
