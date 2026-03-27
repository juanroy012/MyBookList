package com.juanroy.mybooklist.auth.controller;

import com.juanroy.mybooklist.auth.service.AuthenticationService;
import com.juanroy.mybooklist.auth.service.JwtService;
import com.juanroy.mybooklist.auth.dto.request.LoginUserDto;
import com.juanroy.mybooklist.auth.dto.request.RegisterUserDto;
import com.juanroy.mybooklist.auth.dto.request.VerifyUserDto;
import com.juanroy.mybooklist.user.model.User;
import com.juanroy.mybooklist.auth.dto.response.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        authenticationService.verifyUser(verifyUserDto);
        return ResponseEntity.ok("Account verified successfully");
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestBody String email) {
            authenticationService.resetVerificationCode(email);
            return ResponseEntity.ok("Verification code sent");
    }
}
