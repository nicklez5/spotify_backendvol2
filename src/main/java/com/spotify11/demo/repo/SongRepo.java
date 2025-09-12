package com.spotify11.demo.repo;

import com.spotify11.demo.entity.Song;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;


@Repository
public interface SongRepo extends JpaRepository<Song,Integer> {
    public Optional<Song>  findByTitle(String title);
    Optional<Song> findById(Integer id);
    @Query("""
    select s from Song s
    where s.id = :id
      and (:ownerId is null or s.ownerId = :ownerId)
    """)
    public Optional<Song> findByIdWithOptionalOwner(@Param("id") Integer id, @Param("ownerId") Integer ownerId);
    Page<Song> findByTitleContainingIgnoreCase(String q, Pageable pageable);
    Optional<Song> findByIdAndOwnerId(Integer id, Integer ownerId);
    @Query("""
      select distinct s
      from Playlist p
      join p.tracks s
      where p.id = :playlistId
        and p.owner.id = :ownerId
      order by s.id desc
    """)
    List<Song> findSongsInPlaylist(@Param("playlistId") Integer playlistId,
                                  @Param("ownerId") Integer ownerId);
    List<Song> findAllByLibraries_Id(Integer libraryId);


     @Query("""
      select s
      from Library l
      join l.songs s
      where l.owner.id = :ownerId
      order by s.id desc
    """)
    List<Song> findSongsInLibrary(@Param("ownerId") Integer ownerId);


   @Query("""
    select distinct s
    from Song s
    join s.libraries l
    where l.id = :libId
    order by s.id desc
    """)
    List<Song> findByLibraryId(@Param("libId") Integer libId);

     
}
