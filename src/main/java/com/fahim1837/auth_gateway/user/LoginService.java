package com.fahim1837.auth_gateway.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    UserRepository repo;

    public String authenticateUser(LoginPayload payload) {
        System.out.println(payload);
        Optional<User> user = repo.findByUsername(payload.username());
        System.out.println(user);

        if (user.isPresent()) {
            var userObj = user.get();
            validateUser(payload, userObj);
        }

        if (user.isEmpty()) {
            var response = new ResponseObject.ResponseObjectBuilder()
                    .statusCode(400)
                    .message("User doesn't exist")
                    .build();
                

            return response.getResponse(400);

        }
        var response = new ResponseObject.ResponseObjectBuilder()
                .statusCode(200)
                .message("User is okay")
                .build();
        return response.getResponse(200);

    }

    private Map<String, String> validateUser(LoginPayload payload, User user) {
        Map<String, String> map = new HashMap<String, String>();

        if (!payload.username().equals(user.getUsername())) {
            map.put("message", "User Doesn't exist");
        }

        return map;
    }

}
