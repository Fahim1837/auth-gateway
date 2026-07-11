package com.fahim1837.auth_gateway.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> hello() {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", 200);
        responseMap.put("message", "The Server is Healty.");
        return responseMap;
    }
}
