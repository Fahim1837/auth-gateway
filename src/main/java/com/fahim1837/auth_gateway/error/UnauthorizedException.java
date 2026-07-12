package com.fahim1837.auth_gateway.error;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UnauthorizedException extends RuntimeException {
    private String message;

    public UnauthorizedException(String msg) {
        super(msg);
        this.message = msg;
    }
}
