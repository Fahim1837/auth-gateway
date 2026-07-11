package com.fahim1837.auth_gateway.user;

public record RegisterPayload (
    String username,
    String password,
    String confirmPassword,
    String email
) {}
