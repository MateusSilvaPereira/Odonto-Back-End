package com.msp.odonto_pro.exceptions;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handler personalizado para quando um usuário autenticado tenta acessar um
 * recurso sem permissão (403 Forbidden).
 * 
 * Retorna uma resposta JSON padronizada ao invés da página HTML padrão.
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

	@Override
	public void handle(final HttpServletRequest request, final HttpServletResponse response,
			final AccessDeniedException accessDeniedException) throws IOException, ServletException {

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		final String message = String.format(
				"{\"error\":\"Acesso negado\",\"message\":\"Você não tem permissão para acessar este recurso\",\"path\":\"%s\"}",
				request.getRequestURI());

		response.getWriter().write(message);
	}
}