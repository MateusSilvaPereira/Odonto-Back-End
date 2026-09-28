package com.msp.odonto_pro.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro JWT que intercepta todas as requisições HTTP.
 * 
 * Responsabilidades: 1. Extrair o token do header Authorization 2. Validar o
 * token (assinatura e expiração) 3. Carregar o UserDetails completo (com
 * authorities/roles) 4. Colocar a autenticação no SecurityContext
 * 
 * Uma vez que a autenticação está no SecurityContext, os controllers podem
 * usar @PreAuthorize para verificar permissões.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDetailsServiceImp userDetailsService;

	public JwtAuthFilter(final JwtService jwtService, final UserDetailsServiceImp userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Define quais URLs NÃO devem passar pelo filtro.
	 * 
	 * URLs públicas (auth, uploads, swagger) não precisam de token. Requests
	 * OPTIONS (preflight CORS) também são ignoradas.
	 * 
	 * @param request requisição HTTP
	 * @return true se o filtro deve ser pulado
	 */
	@Override
	protected boolean shouldNotFilter(final HttpServletRequest request) {
		final String path = request.getRequestURI();
		final String method = request.getMethod();

		// Ignora preflight CORS
		if ("OPTIONS".equalsIgnoreCase(method)) {
			return true;
		}

		// URLs públicas (exceto /auth/alterar-senha, que exige autenticação —
		// ver SecurityConfig)
		if ("/auth/alterar-senha".equals(path)) {
			return false;
		}
		return path.startsWith("/auth") || path.startsWith("/uploads") || path.startsWith("/swagger-ui")
				|| path.startsWith("/v3/api-docs") || path.startsWith("/public");
	}

	/**
	 * Lógica principal do filtro.
	 * 
	 * Fluxo: 1. Verifica se existe header Authorization com "Bearer " 2. Extrai o
	 * token 3. Extrai o username do token 4. Se ainda não há autenticação no
	 * contexto: a. Carrega o UserDetails do banco b. Valida o token c. Cria o
	 * Authentication com authorities d. Coloca no SecurityContext
	 * 
	 * @param request     requisição HTTP
	 * @param response    resposta HTTP
	 * @param filterChain cadeia de filtros
	 */
	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain filterChain) throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");

		// Se não tem header Authorization ou não começa com "Bearer ", continua
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			// Extrai o token (remove "Bearer ")
			final String token = authHeader.substring(7);
			final String username = this.jwtService.extractUsername(token);

			// Se tem username e ainda não há autenticação no contexto
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				// Carrega o UserDetails completo do banco
				final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

				// Valida o token
				if (this.jwtService.isTokenValid(token, userDetails)) {

					// Cria o Authentication com as authorities (roles + permissions)
					final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities() // 🔑 AQUI ESTÃO AS PERMISSÕES
					);

					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					// Coloca a autenticação no contexto de segurança
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
		} catch (final Exception e) {
			// Log do erro (em produção, use um logger real)
			System.err.println("Erro ao processar token JWT: " + e.getMessage());
			// Não lança exceção aqui para não quebrar a cadeia de filtros
			// O Spring Security tratará como "não autenticado"
		}

		// Continua a cadeia de filtros
		filterChain.doFilter(request, response);
	}
}