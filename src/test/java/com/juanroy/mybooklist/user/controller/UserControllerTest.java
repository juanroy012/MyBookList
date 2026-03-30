package com.juanroy.mybooklist.user.controller;

import com.juanroy.mybooklist.auth.service.JwtService;
import com.juanroy.mybooklist.user.model.User;
import com.juanroy.mybooklist.user.repository.UserRepository;
import com.juanroy.mybooklist.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean UserService userService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setEnabled(true);
    }

    // ── GET /api/v1/users/me ───────────────────────────────────────────────

    @Test
    void getMe_withAuthenticatedUser_returnsUserData() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getMe_withAuthenticatedUser_doesNotExposePassword() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getMe_withAuthenticatedUser_doesNotExposeVerificationCode() throws Exception {
        testUser.setVerificationCode("123456");
        mockMvc.perform(get("/api/v1/users/me").with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationCode").doesNotExist());
    }

    @Test
    void getMe_withoutAuthentication_returns4xx() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().is4xxClientError());
    }
}
