package com.juanroy.mybooklist.auth.dto.response;

public record LoginResponse(
        String token,
        long expiresIn
) {}