package com.spotify11.demo.services;

import com.spotify11.demo.entity.Library;
import com.spotify11.demo.entity.Playlist;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.spotify11.demo.dtos.LoginUserDto;
import com.spotify11.demo.dtos.RegisterUserDto;
import com.spotify11.demo.entity.User;
import com.spotify11.demo.repo.LibraryRepo;
import com.spotify11.demo.repo.UserRepository;

import jakarta.transaction.Transactional;

import java.util.ArrayList;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final LibraryRepo libraryRepo;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            LibraryRepo libraryRepo
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.libraryRepo = libraryRepo;
    }

    @Transactional
    public User signup(RegisterUserDto input) {

        String normalizeName = normalizeFullName(input.getFullName());
        User user = new User();
        user.setFullName(normalizeName);
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        Library lib = new Library();
        lib.setOwner(user);
        user.setLibrary(lib);


        Playlist p = new Playlist();

        p.setPlaylistName("My first playlist");
        user.addPlaylist(p);
        return userRepository.save(user);
    }
    private static String normalizeFullName(String s) {
        if (s == null) return null;
        // trim and collapse internal whitespace so “Jack  son  Lu” == “Jackson Lu”
        return s.trim().replaceAll("\\s+", " ");
    }
    public User authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return userRepository.findByEmail(input.getEmail())
                .orElseThrow();
    }
    public void logout() {
        userRepository.deleteAll();


    }

}

