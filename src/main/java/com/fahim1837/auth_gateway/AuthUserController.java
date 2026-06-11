package com.fahim1837.auth_gateway;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthUserController {

	private final AuthUserRepository authUserRepository;

	public AuthUserController(AuthUserRepository authUserRepository) {
		this.authUserRepository = authUserRepository;
	}

	@GetMapping("/users")
	public List<AuthUser> users() {
		return authUserRepository.findAll();
	}
}
