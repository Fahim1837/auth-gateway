package com.fahim1837.auth_gateway.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fahim1837.auth_gateway.user.LoginPayload;
import com.fahim1837.auth_gateway.user.LoginService;
import com.fahim1837.auth_gateway.user.User;
import com.fahim1837.auth_gateway.user.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    @Autowired
    LoginService service;

    @Autowired
    UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginPayload payload) {

        var user = service.authenticateUser(payload);
        return user;
    }

    @PostMapping("/register") 
    public User register (@RequestBody User user) {

        userService.createUser(user);
        return user;
    }

    @PostMapping("/logout") 
    public String logout () {
        return "This is for logging out from the app";
    }

}
