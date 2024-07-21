package com.example.security.service;

import org.springframework.stereotype.Service;

import com.example.security.model.User;
import com.example.security.repository.UserRepo;
import java.util.List;
import java.util.ArrayList;
@Service
public class UserService {
    private final UserRepo userRepo;

    public UserService(UserRepo userRepo, EmailService emailService) {
        this.userRepo = userRepo;
    }

    public List<User> allUsers(){
        List<User> users = new ArrayList<>();
        userRepo.findAll().forEach(users::add);
        return users;
    }
}
