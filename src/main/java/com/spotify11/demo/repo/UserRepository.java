package com.spotify11.demo.repo;

import java.util.Optional;


import com.spotify11.demo.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.spotify11.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByEmail(String email);
    @Query("select u from User u join fetch u.library where u.email = :email")
    Optional<User> findByEmailWithLibrary(String email);
    @Query("select u from User u join fetch u.library where u.id = :id")
    Optional<User> findByIdWithLibrary(Integer id);

    Optional<User> findByFullName(String fullName);

    boolean existsByFullNameIgnoreCase(String fullName);
}
