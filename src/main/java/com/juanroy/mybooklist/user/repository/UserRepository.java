package com.juanroy.mybooklist.user.repository;

import com.juanroy.mybooklist.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByUsername(String username);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByVerificationCode(String verificationCode);
}
