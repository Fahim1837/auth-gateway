package com.fahim1837.auth_gateway.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fahim1837.auth_gateway.error.UnauthorizedException;

@Service
public class LoginService {

    @Autowired
    UserRepository repo;

    public ResponseEntity<String> authenticateUser(LoginPayload payload) {
        User user = repo.findByUsername(payload.getUsername());

        validateUser(user, payload.getPassword());
        var entity = new ResponseEntity<String>("Hi", HttpStatus.ACCEPTED);
        return entity;
    }

    private void validateUser(User user, String password) {
         if (user == null || !user.checkPassword(password)) {
        throw new UnauthorizedException("Username or Password doesn't exist");  
    }
    }
}
