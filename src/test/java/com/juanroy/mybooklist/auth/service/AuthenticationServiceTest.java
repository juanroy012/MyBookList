package com.juanroy.mybooklist.auth.service;

import com.juanroy.mybooklist.auth.dto.request.LoginUserDto;
import com.juanroy.mybooklist.auth.dto.request.RegisterUserDto;
import com.juanroy.mybooklist.auth.dto.request.VerifyUserDto;
import com.juanroy.mybooklist.email.service.EmailService;
import com.juanroy.mybooklist.user.model.User;
import com.juanroy.mybooklist.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock EmailService emailService;
    @InjectMocks AuthenticationService authenticationService;

    private User testUser;
    private RegisterUserDto registerDto;
    private LoginUserDto loginDto;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setEnabled(true);
        testUser.setVerificationCode("123456");
        testUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        registerDto = new RegisterUserDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password");

        loginDto = new LoginUserDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password");
    }

    // ── signup ──────────────────────────────────────────────────────

    @Test
    void signup_encodesPasswordBeforeSaving() {
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authenticationService.signup(registerDto);

        assertThat(result.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void signup_setsUserAsDisabledWithVerificationCode() {
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authenticationService.signup(registerDto);

        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getVerificationCode()).isNotNull().hasSize(6);
        assertThat(result.getVerificationCodeExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void signup_savesUserAndSendsVerificationEmail() throws MessagingException {
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authenticationService.signup(registerDto);

        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq("test@example.com"), anyString(), anyString());
    }

    // ── authenticate ────────────────────────────────────────────────

    @Test
    void authenticate_withValidVerifiedUser_returnsUser() {
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User result = authenticationService.authenticate(loginDto);

        assertThat(result).isEqualTo(testUser);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_withNonExistentUser_throwsException() {
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authenticate(loginDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void authenticate_withUnverifiedAccount_throwsExceptionWithoutCallingAuthManager() {
        testUser.setEnabled(false);
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authenticationService.authenticate(loginDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not verified");

        verifyNoInteractions(authenticationManager);
    }

    // ── verifyUser ──────────────────────────────────────────────────

    @Test
    void verifyUser_withValidCode_enablesUserAndClearsCode() {
        testUser.setEnabled(false);
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        VerifyUserDto dto = buildVerifyDto("test@example.com", "123456");
        authenticationService.verifyUser(dto);

        assertThat(testUser.isEnabled()).isTrue();
        assertThat(testUser.getVerificationCode()).isNull();
        assertThat(testUser.getVerificationCodeExpiresAt()).isNull();
        verify(userRepository).save(testUser);
    }

    @Test
    void verifyUser_withExpiredCode_throwsException() {
        testUser.setEnabled(false);
        testUser.setVerificationCodeExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authenticationService.verifyUser(buildVerifyDto("test@example.com", "123456")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void verifyUser_withWrongCode_throwsException() {
        testUser.setEnabled(false);
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authenticationService.verifyUser(buildVerifyDto("test@example.com", "999999")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    void verifyUser_withNonExistentUser_throwsException() {
        when(userRepository.findUserByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.verifyUser(buildVerifyDto("ghost@example.com", "123456")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    // ── resetVerificationCode ───────────────────────────────────────

    @Test
    void resetVerificationCode_withUnverifiedUser_generatesNewCodeAndSendsEmail() throws MessagingException {
        testUser.setEnabled(false);
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        authenticationService.resetVerificationCode("test@example.com");

        assertThat(testUser.getVerificationCode()).isNotNull().hasSize(6);
        assertThat(testUser.getVerificationCodeExpiresAt()).isAfter(LocalDateTime.now());
        verify(userRepository).save(testUser);
        verify(emailService).sendVerificationEmail(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void resetVerificationCode_withAlreadyVerifiedUser_throwsException() {
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authenticationService.resetVerificationCode("test@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already verified");
    }

    @Test
    void resetVerificationCode_withNonExistentUser_throwsException() {
        when(userRepository.findUserByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.resetVerificationCode("ghost@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    // ── helpers ─────────────────────────────────────────────────────

    private VerifyUserDto buildVerifyDto(String email, String code) {
        VerifyUserDto dto = new VerifyUserDto();
        dto.setEmail(email);
        dto.setVerificationCode(code);
        return dto;
    }
}

