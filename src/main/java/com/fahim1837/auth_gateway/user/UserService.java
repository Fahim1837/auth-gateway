package com.fahim1837.auth_gateway.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository repo; 

    public User createUser(User user) {

        repo.save(user);
        return user;
    }

    // private bool validateUser() {

    // }
}
