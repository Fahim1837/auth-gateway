package com.fahim1837.auth_gateway.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginPayload {
    @NotEmpty(message = "Username is required")
    String username;

    @NotEmpty(message = "Password is required")
    String password;
}
