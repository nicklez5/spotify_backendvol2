package com.spotify11.demo.repo;




import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spotify11.demo.entity.Library;



@Repository
public interface LibraryRepo extends JpaRepository<Library,Integer> {
     Optional<Library> findByOwnerId(Integer ownerId);
    @Query("select l from Library l left join fetch l.songs where l.owner.id = :ownerId")
    Optional<Library> findByOwnerIdFetchSongs(@Param("ownerId") Integer ownerId);
}
