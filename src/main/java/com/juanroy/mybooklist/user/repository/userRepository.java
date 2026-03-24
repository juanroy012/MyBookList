package com.juanroy.mybooklist.user.repository;

import com.juanroy.mybooklist.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface userRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByVerificationCode(String verificationCode);
}
