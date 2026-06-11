package com.fahim1837.auth_gateway.core.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fahim1837.auth_gateway.core.repository.CoreUserRepository;
import com.fahim1837.auth_gateway.core.repository.RevokedTokenRepository;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final CoreUserRepository userRepository;
	private final RevokedTokenRepository revokedTokenRepository;

	public JwtAuthenticationFilter(
			JwtService jwtService,
			CoreUserRepository userRepository,
			RevokedTokenRepository revokedTokenRepository) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.revokedTokenRepository = revokedTokenRepository;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(7);
		try {
			JwtClaims claims = jwtService.parse(token);
			if ("access".equals(claims.type())
					&& !revokedTokenRepository.existsByTokenHash(HashUtils.sha256(token))) {
				userRepository.findById(claims.userId())
						.filter(user -> user.isActive())
						.ifPresent(user -> {
							AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getEmail());
							UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
									principal,
									null,
									List.of(new SimpleGrantedAuthority("ROLE_USER")));
							SecurityContextHolder.getContext().setAuthentication(authentication);
						});
			}
		} catch (IllegalArgumentException ignored) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}
}
