package com.fahim1837.auth_gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AuthUserSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(AuthUserSeeder.class);

	private final AuthUserRepository authUserRepository;

	public AuthUserSeeder(AuthUserRepository authUserRepository) {
		this.authUserRepository = authUserRepository;
	}

	@Override
	public void run(String... args) {
		if (authUserRepository.count() > 0) {
			log.info("auth_users table already has data");
			return;
		}

		authUserRepository.save(new AuthUser("Fahim", "fahim@example.com"));
		authUserRepository.save(new AuthUser("Nadia", "nadia@example.com"));
		authUserRepository.save(new AuthUser("Karim", "karim@example.com"));

		log.info("Seeded 3 rows into auth_users");
	}
}
