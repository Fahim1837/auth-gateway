package com.fahim1837.auth_gateway.user;

public record LoginPayload (
    String username,
    String password
) {}
