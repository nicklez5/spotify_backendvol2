package com.spotify11.demo.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.spotify11.demo.dtos.PlaylistSummaryDto;
import com.spotify11.demo.entity.Playlist;
import com.spotify11.demo.entity.Song;
import com.spotify11.demo.enums.Visibility;

import org.springframework.lang.NonNull;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepo extends JpaRepository<Playlist, Integer> {

    @EntityGraph(attributePaths = {"owner", "tracks"})
    @Query("select distinct p from Playlist p where p.id = :id")
    Optional<Playlist> findById(@NonNull Integer id);

    @EntityGraph(attributePaths = {"owner", "tracks"})
    @Query("select distinct p from Playlist p where p.owner.id = :ownerId order by p.id desc")
    List<Playlist> findByOwnerIdOrderByIdDesc(Integer ownerId);

    @Query("select distinct p from Playlist p join p.tracks s where lower(s.title) like lower(:pattern) escape '\\' or lower(s.artist) like lower(:pattern) escape '\\'")
    List<Playlist> findPlaylistsBySongLike(@Param("pattern") String pattern);

    @Query("select distinct p from Playlist p join p.tracks s where lower(s.artist) like lower(:pattern) escape '\\'")
    List<Playlist> findPlaylistsBySongArtistLike(@Param("pattern") String pattern);

    Playlist findByPlaylistName(String playlist_name);

    List<Playlist> findByOwnerIdOrderByIdAsc(int ownerId);
    @EntityGraph(attributePaths = {"owner","tracks"})
    Optional<Playlist> findByIdAndOwnerId(Integer id, Integer ownerId);
    
    long countByOwnerId(Integer ownerId);

    @EntityGraph(attributePaths = {"owner", "tracks"})
    @Query("select distinct p from Playlist p where p.id = :id")
    Optional<Playlist> findWithTracksById(Integer id );

      @Modifying // from org.springframework.data.jpa.repository.Modifying
    @Query(value = "DELETE FROM playlist_tracks WHERE song_id = :songId", nativeQuery = true)
    int unlinkSongFromAllPlaylists(@Param("songId") Integer songId);


    List<Playlist> findByOwnerIdAndVisibilityOrderByIdDesc(Integer ownerId, Visibility visibility);

    @Query("""
    select new com.spotify11.demo.dtos.PlaylistSummaryDto(
      p.id,
      p.playlistName,            
      p.owner.fullName,
      size(p.tracks),
      cast(p.visibility as string)
    )
    from Playlist p
    where p.owner.id = :userId
    order by p.id desc
  """)
    List<PlaylistSummaryDto> findSummariesByOwnerId(@Param("userId") Integer userId);

    @Query("""
    select new com.spotify11.demo.dtos.PlaylistSummaryDto(
      p.id,
      p.playlistName,
      p.owner.fullName,
      size(p.tracks),
      cast(p.visibility as string)
    )
    from Playlist p
    where p.owner.id = :userId and p.visibility = :visibility
    order by p.id desc
  """)
    List<PlaylistSummaryDto> findSummariesByOwnerIdAndVisibility(
      @Param("userId") Integer userId,
      @Param("visibility") Visibility visibility);
}
