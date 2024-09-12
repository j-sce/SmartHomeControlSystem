package com.smart.home.userservice.service;

import com.smart.home.userservice.model.LoginDTO;
import com.smart.home.userservice.model.TokenValidationRequest;
import com.smart.home.userservice.model.User;

import java.util.List;

public interface UserService {

    String login(LoginDTO loginDTO);

    User createUser(User user);

    User getUserById(Long userId);

    void deleteUserById(Long userId);

    User updateUserById(Long userId, User userUpdate);

    List<User> getAllUsers();

    User getProfile();

    Boolean validateToken(TokenValidationRequest request);
}
