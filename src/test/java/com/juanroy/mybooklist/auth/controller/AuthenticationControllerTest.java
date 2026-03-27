package com.juanroy.mybooklist.auth.controller;

import tools.jackson.databind.ObjectMapper;
import com.juanroy.mybooklist.auth.dto.request.LoginUserDto;
import com.juanroy.mybooklist.auth.dto.request.RegisterUserDto;
import com.juanroy.mybooklist.auth.dto.request.VerifyUserDto;
import com.juanroy.mybooklist.auth.service.AuthenticationService;
import com.juanroy.mybooklist.auth.service.JwtService;
import com.juanroy.mybooklist.user.model.User;
import com.juanroy.mybooklist.user.repository.UserRepository;
import com.juanroy.mybooklist.config.ApplicationConfiguration;
import com.juanroy.mybooklist.config.SecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// /auth/** is permitAll() in SecurityConfiguration so no auth header is needed here
@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfiguration.class, ApplicationConfiguration.class})
class AuthenticationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // Service mocks
    @MockitoBean AuthenticationService authenticationService;
    @MockitoBean JwtService jwtService;

    // UserRepository is needed by ApplicationConfiguration (loaded by @WebMvcTest)
    @MockitoBean UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setEnabled(true);
    }

    // ── POST /auth/signup ───────────────────────────────────────────

    @Test
    void signup_withValidBody_returns200AndUserData() throws Exception {
        when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(testUser);

        RegisterUserDto dto = new RegisterUserDto();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"))
                // password must be hidden via @JsonIgnore
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void signup_whenServiceThrows_returns4xx() throws Exception {
        when(authenticationService.signup(any())).thenThrow(new RuntimeException("Email already in use"));

        RegisterUserDto dto = new RegisterUserDto();
        dto.setUsername("x");
        dto.setEmail("dup@example.com");
        dto.setPassword("pass");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());
    }

    // ── POST /auth/login ────────────────────────────────────────────

    @Test
    void login_withValidCredentials_returnsTokenAndExpiry() throws Exception {
        when(authenticationService.authenticate(any(LoginUserDto.class))).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn("mocked.jwt.token");
        when(jwtService.getExpirationTime()).thenReturn(3600000L);

        LoginUserDto dto = new LoginUserDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.expiresIn").value(3600000));
    }

    @Test
    void login_withInvalidCredentials_returns4xx() throws Exception {
        when(authenticationService.authenticate(any())).thenThrow(new RuntimeException("User not found"));

        LoginUserDto dto = new LoginUserDto();
        dto.setEmail("wrong@example.com");
        dto.setPassword("wrong");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void login_withUnverifiedAccount_returns4xx() throws Exception {
        when(authenticationService.authenticate(any()))
                .thenThrow(new RuntimeException("Account not verified. Please verify your account"));

        LoginUserDto dto = new LoginUserDto();
        dto.setEmail("unverified@example.com");
        dto.setPassword("password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());
    }

    // ── POST /auth/verify ───────────────────────────────────────────

    @Test
    void verify_withValidCode_returns200() throws Exception {
        doNothing().when(authenticationService).verifyUser(any(VerifyUserDto.class));

        VerifyUserDto dto = new VerifyUserDto();
        dto.setEmail("test@example.com");
        dto.setVerificationCode("123456");

        mockMvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Account verified successfully"));
    }

    @Test
    void verify_withExpiredCode_returns400WithMessage() throws Exception {
        doThrow(new RuntimeException("Verification code has expired"))
                .when(authenticationService).verifyUser(any(VerifyUserDto.class));

        VerifyUserDto dto = new VerifyUserDto();
        dto.setEmail("test@example.com");
        dto.setVerificationCode("000000");

        mockMvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Verification code has expired"));
    }

    @Test
    void verify_withInvalidCode_returns400() throws Exception {
        doThrow(new RuntimeException("Invalid verification code"))
                .when(authenticationService).verifyUser(any(VerifyUserDto.class));

        VerifyUserDto dto = new VerifyUserDto();
        dto.setEmail("test@example.com");
        dto.setVerificationCode("wrong");

        mockMvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ── POST /auth/resend ───────────────────────────────────────────

    @Test
    void resend_withUnverifiedEmail_returns200() throws Exception {
        doNothing().when(authenticationService).resetVerificationCode(anyString());

        mockMvc.perform(post("/auth/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"test@example.com\""))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification code sent"));
    }

    @Test
    void resend_withAlreadyVerifiedEmail_returns400() throws Exception {
        doThrow(new RuntimeException("Account is already verified"))
                .when(authenticationService).resetVerificationCode(anyString());

        mockMvc.perform(post("/auth/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"verified@example.com\""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Account is already verified"));
    }
}

