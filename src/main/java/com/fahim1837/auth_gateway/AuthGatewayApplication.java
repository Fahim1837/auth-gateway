package com.fahim1837.auth_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class AuthGatewayApplication {

	public static void main(String[] args) {
		System.out.println("Hello World");
		SpringApplication.run(AuthGatewayApplication.class, args);
	}

}

// @RestController
// public class HelloController {

//     private static final Logger log = LoggerFactory.getLogger(HelloController.class);

//     @GetMapping("/")
//     public String hello() {
//         log.info("Hit / endpoint");   // shows because com.fahim1837 = DEBUG
//         return "Hello World";
//     }
// }

