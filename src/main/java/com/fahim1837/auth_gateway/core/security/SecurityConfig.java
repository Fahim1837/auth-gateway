package com.fahim1837.auth_gateway.core.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
			OAuth2LoginFailureHandler oauth2LoginFailureHandler,
			ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/",
								"/oauth2/**",
								"/login/oauth2/**",
								"/auth/register",
								"/auth/login",
								"/auth/refresh-token",
								"/auth/email/verify",
								"/auth/email/resend-verification",
								"/auth/password/forgot",
								"/auth/password/reset",
								"/auth/token/introspect",
								"/auth/token/revoke")
						.permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		if (clientRegistrationRepository.getIfAvailable() != null) {
			http.oauth2Login(oauth2 -> oauth2
					.successHandler(oauth2LoginSuccessHandler)
					.failureHandler(oauth2LoginFailureHandler));
		}

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService() {
		return username -> {
			throw new UsernameNotFoundException(username);
		};
	}
}
