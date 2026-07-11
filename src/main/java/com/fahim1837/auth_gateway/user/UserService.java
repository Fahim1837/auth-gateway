package com.fahim1837.auth_gateway.user;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository repo; 

    public User createUser() {
        UUID id = new UUID(0, 0);
        User user = new User();
        user.setId(id);
        user.setName("Fahim Ahmed");
        repo.save(user);
        return user;
    }
}
