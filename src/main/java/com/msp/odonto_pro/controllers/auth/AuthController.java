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

/**
 * Controller responsável por autenticação e registro.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtService jwtService;
	private final UserDetailsServiceImp userDetailsService;
	private final AuthenticationManager authenticationManager;

	/**
	 * Registra um novo usuário no sistema.
	 * 
	 * @param request dados do novo usuário
	 * @return resposta vazia com status 200
	 */
	@Operation(summary = "Registrar novo usuário")
	@PostMapping("/register")
	public ResponseEntity<Void> register(@RequestBody final RegisterRequest request) {
		this.authService.register(request);
		return ResponseEntity.ok().build();
	}

	/**
	 * Autentica um usuário e gera um token JWT.
	 * 
	 * Fluxo: 1. Valida credenciais via AuthenticationManager 2. Coloca a
	 * autenticação no SecurityContext 3. Carrega o UserDetails completo 4. Gera o
	 * token JWT (incluindo role e permissões) 5. Retorna token + dados do usuário
	 * 
	 * @param request credenciais (username + password)
	 * @return token JWT e dados do usuário
	 */
	@Operation(summary = "Autenticar usuário e gerar token")
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody final LoginRequest request) {
		// 1. Autentica as credenciais
		final Authentication authentication = this.authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		// 2. Coloca no contexto de segurança
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// 3. Carrega o UserDetails completo (com authorities)
		final UserDetails userDetails = this.userDetailsService.loadUserByUsername(request.getUsername());

		// 4. Gera o token JWT (passando UserDetails completo)
		final String token = this.jwtService.generateToken(userDetails);

		// 5. Monta a resposta
		final Usuario usuario = (Usuario) userDetails;
		return ResponseEntity.ok(new LoginResponse(token, usuario));
	}
}