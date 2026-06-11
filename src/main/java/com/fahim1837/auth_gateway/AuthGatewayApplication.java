package com.fahim1837.auth_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthGatewayApplication {

	public static void main(String[] args) {
		System.setProperty("debug", System.getProperty("debug", "false"));
		SpringApplication.run(AuthGatewayApplication.class, args);
	}

}
