package com.juanroy.mybooklist.user.service;

import com.juanroy.mybooklist.user.model.User;
import com.juanroy.mybooklist.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
