package com.spotify11.demo.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.spotify11.demo.dtos.PlaylistSummaryDto;
import com.spotify11.demo.entity.Playlist;
import com.spotify11.demo.enums.Visibility;

import org.springframework.lang.NonNull;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepo extends JpaRepository<Playlist, Integer> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"owner", "tracks"})
    Optional<Playlist> findById(@NonNull Integer id);

    @EntityGraph(attributePaths = {"owner", "tracks"})
    List<Playlist> findByOwnerIdOrderByIdDesc(Integer ownerId);


    Playlist findByPlaylistName(String playlist_name);

    List<Playlist> findByOwnerIdOrderByIdAsc(int ownerId);

    Optional<Playlist> findByIdAndOwnerId(Integer id, Integer ownerId);
    
    long countByOwnerId(Integer ownerId);

    @EntityGraph(attributePaths = {"owner", "tracks"})
    Optional<Playlist> findWithTracksById(Integer id );

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
