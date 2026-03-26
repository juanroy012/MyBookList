package com.juanroy.mybooklist.user.dto.response;

public record LoginResponse(
        String token,
        long expiresIn
) {}