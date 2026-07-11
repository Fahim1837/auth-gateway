package com.fahim1837.auth_gateway.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fahim1837.auth_gateway.user.LoginPayload;
import com.fahim1837.auth_gateway.user.LoginService;
import com.fahim1837.auth_gateway.user.User;
import com.fahim1837.auth_gateway.user.UserService;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    @Autowired
    LoginService service;

    @Autowired
    UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginPayload payload) {

        var user = service.authenticateUser(payload);
        var entity = new ResponseEntity<String>(user, HttpStatus.BAD_REQUEST);
        return entity;
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
