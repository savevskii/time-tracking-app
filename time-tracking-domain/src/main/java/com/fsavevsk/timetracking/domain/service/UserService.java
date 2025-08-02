package com.fsavevsk.timetracking.domain.service;

import com.fsavevsk.timetracking.domain.model.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();

    User getUserById(Long id);

    User findByEmail(String email);

    User addUser(User newUser);

    User updateUserById(Long id, User updatedUser);

    void deleteUserById(Long id);

    boolean existsByEmail(String email);

}

