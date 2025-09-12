package com.spotify11.demo.services;

import com.spotify11.demo.dtos.LoginUserDto;
import com.spotify11.demo.dtos.RegisterUserDto;
import com.spotify11.demo.dtos.UserPublicDto;
import com.spotify11.demo.entity.*;


import com.spotify11.demo.exception.UserException;

import com.spotify11.demo.repo.PlaylistRepo;
import com.spotify11.demo.repo.SongRepo;
import com.spotify11.demo.repo.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.springframework.http.ResponseEntity.ok;

@Service
public class UserImpl implements UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PlaylistRepo playlistRepo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserImpl(UserRepository userRepo) {
        this.userRepo = userRepo;

    }


    @Transactional
    public User addUser(RegisterUserDto input) throws UserException {
        User xyz = new User();
        xyz.setEmail(input.getEmail());
        xyz.setPassword(encoder.encode(input.getPassword()));
        xyz.setFullName(input.getFullName());
        this.userRepo.save(xyz);
        return xyz;
    }

    @Transactional
    public List<UserPublicDto> getAllUsers() {
        return userRepo.findAll().stream()
            .map(u -> UserPublicDto.from(u, (int) playlistRepo.countByOwnerId(u.getId())))
            .toList();

    }


    @Transactional
    public User updateUser(String fullName, String password, String email) throws UserException {
        Optional<User> xyz123 = userRepo.findByEmail(email);
        if(xyz123.isEmpty()){
            throw new UserException("User not found with email: " + email);
        }
        Optional<User> existingUser = userRepo.findByFullName(fullName);
        if(existingUser.isPresent() && !existingUser.get().getEmail().equals(email)){
            throw new UserException("Full name already taken");
        }
        xyz123.get().setFullName(fullName);
       // only update password if not empty/null
        if (password != null && !password.isBlank()) {
            xyz123.get().setPassword(encoder.encode(password));
        }
        this.userRepo.save(xyz123.get());
        return xyz123.get();


    }

    public UserPublicDto getUser(String fullName) throws UserException{
        Optional<User> xyz123 = userRepo.findByFullName(fullName);
        if(xyz123.isPresent()){
            return UserPublicDto.from(xyz123.get(), (int) playlistRepo.countByOwnerId(xyz123.get().getId()));
        }
        return null;
    }

    public User readUser(String email) throws UserException {
        if(userRepo.findByEmail(email).isPresent()) {
            return userRepo.findByEmail(email).get();
        }else {
            throw new UserException("User not found");
        }

    }
    // id is the one we are looking to delete
    // uuID is the parent
    public User deleteUser(String email) throws UserException {
        if(userRepo.findByEmail(email).isPresent()) {
            userRepo.delete(userRepo.findByEmail(email).get());
            return userRepo.findByEmail(email).get();

        }

        return null;



    }


    public User authenticate(LoginUserDto input) throws UserException {
        if(userRepo.findByEmail(input.getEmail()).isPresent()) {
            User user1 = userRepo.findByEmail(input.getEmail()).get();
            if(user1.isEnabled()){
                return user1;
            }else{
                throw new UserException("Could not authenticate");
            }
        }
        return null;

    }


}





