package com.msp.odonto_pro.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handler global de exceções.
 *
 * Padroniza todas as respostas de erro da API como {"message": "texto amigável"},
 * conforme esperado pelo front-end. Também garante que erros de regra de negócio
 * (ex: "não pode excluir a própria conta") não sejam confundidos com erros de
 * sessão (401), evitando logout indevido no front.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(final NotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(ex.getMessage()));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, String>> handleAccessDenied(final AccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(body("Você não tem permissão para acessar este recurso"));
	}

	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<Map<String, String>> handleDisabled(final DisabledException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body("Usuário inativo"));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<Map<String, String>> handleAuthentication(final AuthenticationException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body("Usuário ou senha inválidos"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidation(final MethodArgumentNotValidException ex) {
		final String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).orElse("Dados inválidos");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(message));
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Map<String, String>> handleRuntime(final RuntimeException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleGeneric(final Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body("Erro interno do servidor"));
	}

	private Map<String, String> body(final String message) {
		final Map<String, String> body = new LinkedHashMap<>();
		body.put("message", message);
		return body;
	}
}
