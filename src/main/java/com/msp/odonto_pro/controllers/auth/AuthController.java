package com.msp.odonto_pro.controllers.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msp.odonto_pro.dtos.auth.AuthRequest;
import com.msp.odonto_pro.entity.Usuario;
import com.msp.odonto_pro.security.JwtService;
import com.msp.odonto_pro.services.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final JwtService jwtService;
	private final AuthService authService;

	public AuthController(JwtService jwtService, AuthService authService) {
		this.jwtService = jwtService;
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {

		Usuario usuario = authService.autenticar(request.getUsername(), request.getPassword());

		String token = jwtService.generateToken(usuario.getUsername());

		return ResponseEntity.ok(new AuthResponse(token));
	}

	@PostMapping(value = "/salvar")
	public ResponseEntity<Usuario> criarUsuario(@RequestBody Usuario usuario) {
		return ResponseEntity.ok().body(authService.criarUsuario(usuario));
	}
}