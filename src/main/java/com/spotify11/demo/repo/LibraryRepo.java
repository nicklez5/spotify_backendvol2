package com.spotify11.demo.repo;




import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spotify11.demo.entity.Library;



@Repository
public interface LibraryRepo extends JpaRepository<Library,Integer> {

    @EntityGraph(attributePaths = "songs")
     Optional<Library> findByOwnerId(Integer ownerId);

    @Query("""
    select distinct l
    from User u
    join u.library l
    left join fetch l.songs
    where u.id = :ownerId
    """)
    Optional<Library> findByOwnerIdFetchSongs(@Param("ownerId") Integer ownerId);

    @Query(value = "select library_id from users where id = :ownerId", nativeQuery = true)
    Integer findLibraryIdForUser(@Param("ownerId") Integer ownerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = "insert into library_songs (library_id, song_id) " +
                "values (:libId, :songId) " +
                "on conflict (library_id, song_id) do nothing",
        nativeQuery = true
    )
    int linkSong(@Param("libId") Integer libId, @Param("songId") Integer songId);

    // optional: create empty library row (if you allow creating on first add)
    @Modifying
    @Query(value = "insert into library default values", nativeQuery = true)
    int createLibraryRow(); // use only if you need to seed one

}
