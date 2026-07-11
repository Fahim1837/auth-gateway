package com.fahim1837.auth_gateway.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fahim1837.auth_gateway.user.User;
import com.fahim1837.auth_gateway.user.UserService;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    @Autowired
    UserService service;

    @PostMapping("/login")
    public ResponseEntity<User> login() {
        User user = service.createUser();
        System.out.print(user);
        var entity = new ResponseEntity<User>(user, HttpStatus.CREATED);
        return entity;
    }

    @PostMapping("/register") 
    public String register () {
        return "This is the user registration";
    }

    @PostMapping("/logout") 
    public String logout () {
        return "This is for logging out from the app";
    }

}
