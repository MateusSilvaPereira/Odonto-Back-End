package com.msp.odonto_pro.controllers.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msp.odonto_pro.dtos.auth.LoginRequest;
import com.msp.odonto_pro.dtos.auth.LoginResponse;
import com.msp.odonto_pro.dtos.auth.RegisterRequest;
import com.msp.odonto_pro.entity.Usuario;
import com.msp.odonto_pro.security.JwtService;
import com.msp.odonto_pro.security.UserDetailsServiceImp;
import com.msp.odonto_pro.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtService jwtService;
	private final UserDetailsServiceImp userDetailsService;
	private final AuthenticationManager authenticationManager;

	@Operation(summary = "Registrar novo usuário")
	@PostMapping("/register")
	public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
		authService.register(request);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Autenticar usuário e gerar token")
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
		String token = jwtService.generateToken(userDetails.getUsername());

		Usuario usuario = (Usuario) userDetails;
		return ResponseEntity.ok(new LoginResponse(token, usuario));
	}
}