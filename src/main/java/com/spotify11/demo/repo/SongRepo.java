package com.spotify11.demo.repo;

import com.spotify11.demo.entity.Song;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;


@Repository
public interface SongRepo extends JpaRepository<Song,Integer> {
    public Optional<Song>  findByTitle(String title);
    Page<Song> findByTitleContainingIgnoreCase(String q, Pageable pageable);
    Optional<Song> findByIdAndOwnerId(Integer id, Integer ownerId);
    @Query("""
        select s from Song s
        join s.playlist p
        where p.id = :playlistId and s.ownerId = :ownerId
        order by s.id desc
    """)
    List<Song> findByPlaylistAndOwner(@Param("playlistId") Integer playlistId,
                                      @Param("ownerId") Integer ownerId);
    List<Song> findAllByLibrary_Id(Integer libraryId);
    List<Song> findByLibraryOwnerId(Integer ownerId);
}
