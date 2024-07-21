package com.example.security.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.example.security.model.User;

public interface UserRepo extends CrudRepository<User,Long>{
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationCode(String verificationCode);
}
