package com.spotify11.demo.services;

import com.spotify11.demo.dtos.LoginUserDto;
import com.spotify11.demo.dtos.RegisterUserDto;
import com.spotify11.demo.entity.*;

import com.spotify11.demo.exception.UserException;

import java.util.List;

import com.spotify11.demo.exception.PlaylistException;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

    User addUser(RegisterUserDto input) throws UserException;
    List<User> getAllUser();

    User updateUser(String fullName, String password, String email) throws UserException;
    User readUser(String email) throws UserException;
    User deleteUser(String email) throws UserException;


    User authenticate(LoginUserDto input) throws UserException;
}
