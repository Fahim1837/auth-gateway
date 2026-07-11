package com.fahim1837.auth_gateway.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    
    @PostMapping("/login")
    public String login() {
        return "This is the login";
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
