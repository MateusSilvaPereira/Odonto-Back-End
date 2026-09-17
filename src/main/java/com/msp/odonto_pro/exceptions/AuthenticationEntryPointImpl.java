package com.msp.odonto_pro.exceptions;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handler personalizado para quando um usuário NÃO autenticado tenta acessar um
 * recurso protegido (401 Unauthorized).
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

	@Override
	public void commence(final HttpServletRequest request, final HttpServletResponse response,
			final AuthenticationException authException) throws IOException, ServletException {

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		final String message = String.format(
				"{\"error\":\"Não autenticado\",\"message\":\"Você precisa fazer login para acessar este recurso\",\"path\":\"%s\"}",
				request.getRequestURI());

		response.getWriter().write(message);
	}
}