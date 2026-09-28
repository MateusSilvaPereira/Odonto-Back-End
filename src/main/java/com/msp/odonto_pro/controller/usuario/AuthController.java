package com.msp.odonto_pro.controller.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msp.odonto_pro.dto.usuario.AlterarSenhaRequest;
import com.msp.odonto_pro.dto.usuario.EsqueciSenhaRequest;
import com.msp.odonto_pro.dto.usuario.LoginRequest;
import com.msp.odonto_pro.dto.usuario.LoginResponse;
import com.msp.odonto_pro.dto.usuario.RedefinirSenhaRequest;
import com.msp.odonto_pro.dto.usuario.RegisterRequest;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.service.usuario.AuthService;
import com.msp.odonto_pro.service.usuario.PasswordResetService;
import com.msp.odonto_pro.web.ApiPaths;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller responsável por autenticação e registro.
 */
@RestController
@RequestMapping(ApiPaths.V1 + "/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final PasswordResetService passwordResetService;

	@Operation(summary = "Registrar novo usuário")
	@PostMapping("/register")
	public ResponseEntity<Void> register(@Valid @RequestBody final RegisterRequest request) {
		this.authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@Operation(summary = "Autenticar usuário e gerar token")
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody final LoginRequest request) {
		return ResponseEntity.ok(this.authService.login(request));
	}

	/**
	 * Sempre responde sucesso genérico, exista ou não o e-mail — evita
	 * confirmar pra quem pede se um endereço está cadastrado (ver
	 * PasswordResetService.solicitarRedefinicao).
	 */
	@Operation(summary = "Solicitar redefinição de senha por e-mail.")
	@PostMapping("/esqueci-senha")
	public ResponseEntity<Void> esqueciSenha(@Valid @RequestBody final EsqueciSenhaRequest request) {
		this.passwordResetService.solicitarRedefinicao(request.getEmail());
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Redefinir senha a partir do token recebido por e-mail.")
	@PostMapping("/redefinir-senha")
	public ResponseEntity<Void> redefinirSenha(@Valid @RequestBody final RedefinirSenhaRequest request) {
		this.passwordResetService.redefinirSenha(request.getToken(), request.getNovaSenha());
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Alterar a própria senha (autenticado).")
	@PostMapping("/alterar-senha")
	public ResponseEntity<Void> alterarSenha(@Valid @RequestBody final AlterarSenhaRequest request,
			final Authentication authentication) {
		final Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
		this.passwordResetService.alterarSenha(usuarioLogado, request.getSenhaAtual(), request.getNovaSenha());
		return ResponseEntity.ok().build();
	}
}
