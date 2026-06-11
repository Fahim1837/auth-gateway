package com.fahim1837.auth_gateway.core.api;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fahim1837.auth_gateway.core.security.AuthenticatedUser;
import com.fahim1837.auth_gateway.core.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public AuthTokensResponse register(@RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
		return authService.register(request.name(), request.email(), request.password(), userAgent(servletRequest));
	}

	@PostMapping("/login")
	public AuthTokensResponse login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
		return authService.login(
				request.email(),
				request.password(),
				request.twoFactorCode(),
				userAgent(servletRequest));
	}

	@PostMapping("/logout")
	public MessageResponse logout(
			@AuthenticationPrincipal AuthenticatedUser principal,
			@RequestBody(required = false) LogoutRequest request,
			HttpServletRequest servletRequest) {
		String refreshToken = request == null ? null : request.refreshToken();
		return authService.logout(principal, bearerToken(servletRequest), refreshToken);
	}

	@PostMapping("/refresh-token")
	public AuthTokensResponse refreshToken(@RequestBody RefreshTokenRequest request, HttpServletRequest servletRequest) {
		return authService.refresh(request.refreshToken(), userAgent(servletRequest));
	}

	@GetMapping("/me")
	public UserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
		return authService.me(principal);
	}

	@PatchMapping("/me")
	public UserResponse updateMe(
			@AuthenticationPrincipal AuthenticatedUser principal,
			@RequestBody UpdateMeRequest request) {
		return authService.updateMe(principal, request.name());
	}

	@DeleteMapping("/me")
	public MessageResponse deactivateMe(@AuthenticationPrincipal AuthenticatedUser principal) {
		return authService.deactivateMe(principal);
	}

	@PostMapping("/email/verify")
	public MessageResponse verifyEmail(@RequestBody EmailCodeRequest request) {
		return authService.verifyEmail(request.email(), request.code());
	}

	@PostMapping("/email/resend-verification")
	public CodeResponse resendVerification(@RequestBody EmailRequest request) {
		return authService.resendVerification(request.email());
	}

	@PostMapping("/password/forgot")
	public CodeResponse forgotPassword(@RequestBody EmailRequest request) {
		return authService.forgotPassword(request.email());
	}

	@PostMapping("/password/reset")
	public MessageResponse resetPassword(@RequestBody PasswordResetRequest request) {
		return authService.resetPassword(request.email(), request.code(), request.newPassword());
	}

	@PostMapping("/password/change")
	public MessageResponse changePassword(
			@AuthenticationPrincipal AuthenticatedUser principal,
			@RequestBody PasswordChangeRequest request) {
		return authService.changePassword(principal, request.currentPassword(), request.newPassword());
	}

	@GetMapping("/sessions")
	public List<SessionResponse> sessions(@AuthenticationPrincipal AuthenticatedUser principal) {
		return authService.sessions(principal);
	}

	@DeleteMapping("/sessions/{sessionId}")
	public MessageResponse revokeSession(
			@AuthenticationPrincipal AuthenticatedUser principal,
			@PathVariable Long sessionId) {
		return authService.revokeSession(principal, sessionId);
	}

	@DeleteMapping("/sessions")
	public MessageResponse revokeAllSessions(@AuthenticationPrincipal AuthenticatedUser principal) {
		return authService.revokeAllSessions(principal);
	}

	@PostMapping("/2fa/setup")
	public TwoFactorSetupResponse setupTwoFactor(@AuthenticationPrincipal AuthenticatedUser principal) {
		return authService.setupTwoFactor(principal);
	}

	@PostMapping("/2fa/verify")
	public MessageResponse verifyTwoFactor(
			@AuthenticationPrincipal AuthenticatedUser principal,
			@RequestBody CodeOnlyRequest request) {
		return authService.verifyTwoFactor(principal, request.code());
	}

	@PostMapping("/2fa/disable")
	public MessageResponse disableTwoFactor(
			@AuthenticationPrincipal AuthenticatedUser principal,
			@RequestBody PasswordOnlyRequest request) {
		return authService.disableTwoFactor(principal, request.password());
	}

	@PostMapping("/token/introspect")
	public TokenIntrospectionResponse introspect(@RequestBody TokenRequest request) {
		return authService.introspect(request.token());
	}

	@PostMapping("/token/revoke")
	public MessageResponse revokeToken(@RequestBody TokenRequest request) {
		return authService.revokeToken(request.token());
	}

	private String userAgent(HttpServletRequest request) {
		return request.getHeader("User-Agent");
	}

	private String bearerToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith("Bearer ")) {
			return null;
		}

		return header.substring(7);
	}

	public record RegisterRequest(String name, String email, String password) {
	}

	public record LoginRequest(String email, String password, String twoFactorCode) {
	}

	public record LogoutRequest(String refreshToken) {
	}

	public record RefreshTokenRequest(String refreshToken) {
	}

	public record UpdateMeRequest(String name) {
	}

	public record EmailRequest(String email) {
	}

	public record EmailCodeRequest(String email, String code) {
	}

	public record PasswordResetRequest(String email, String code, String newPassword) {
	}

	public record PasswordChangeRequest(String currentPassword, String newPassword) {
	}

	public record CodeOnlyRequest(String code) {
	}

	public record PasswordOnlyRequest(String password) {
	}

	public record TokenRequest(String token) {
	}
}
