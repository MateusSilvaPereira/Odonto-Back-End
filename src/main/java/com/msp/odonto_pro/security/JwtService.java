package com.msp.odonto_pro.security;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.msp.odonto_pro.entity.Usuario;
import com.msp.odonto_pro.enums.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Serviço responsável por gerar e validar tokens JWT.
 * 
 * O token JWT contém: - subject: username do usuário - claim "role": papel do
 * usuário (para uso no frontend) - claim "authorities": lista de permissões
 * (opcional) - issuedAt: data de emissão - expiration: data de expiração
 * 
 * IMPORTANTE: No Java 11, não usamos pattern matching (instanceof + cast).
 * Fazemos o cast tradicional com instanceof.
 */
@Service
public class JwtService {

	@Value("${jwt.secret}")
	private String secretKey;

	/**
	 * Tempo de expiração do token: 1 hora (em milissegundos)
	 */
	private static final long EXPIRATION_TIME = 1000L * 60L * 60L;

	/**
	 * Gera um token JWT para o usuário autenticado.
	 * 
	 * O token inclui: - Subject: username (identificador único) - Claim "role":
	 * nome do Role (ex: "ADMIN") - Claim "nome": nome do usuário (para exibição no
	 * frontend)
	 * 
	 * @param userDetails detalhes do usuário autenticado
	 * @return token JWT assinado
	 * @throws IllegalArgumentException se userDetails não for um Usuario
	 */
	public String generateToken(final UserDetails userDetails) {
		// No Java 11, usamos instanceof tradicional
		if (!(userDetails instanceof Usuario)) {
			throw new IllegalArgumentException("UserDetails deve ser uma instância de Usuario");
		}

		final Usuario usuario = (Usuario) userDetails;
		final Date now = new Date();
		final Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

		return Jwts.builder().setSubject(usuario.getUsername())
				// Adiciona o role como claim (útil para o frontend)
				.claim("role", usuario.getRole().name())
				// Adiciona o nome do usuário (útil para o frontend)
				.claim("nome", usuario.getNome())
				// Adiciona o ID do usuário
				.claim("id", usuario.getId()).setIssuedAt(now).setExpiration(expiration)
				.signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
	}

	/**
	 * Extrai o username (subject) do token.
	 * 
	 * @param token token JWT
	 * @return username contido no token
	 */
	public String extractUsername(final String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Extrai o Role do token.
	 * 
	 * @param token token JWT
	 * @return Role do usuário
	 */
	public Role extractRole(final String token) {
		final String roleName = extractClaim(token, claims -> claims.get("role", String.class));
		return Role.valueOf(roleName);
	}

	/**
	 * Extrai um claim específico do token.
	 * 
	 * @param <T>      tipo do claim
	 * @param token    token JWT
	 * @param resolver função que extrai o claim desejado
	 * @return valor do claim
	 */
	public <T> T extractClaim(final String token, final Function<Claims, T> resolver) {
		final Claims claims = Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();

		return resolver.apply(claims);
	}

	/**
	 * Valida se o token é válido para o usuário informado.
	 * 
	 * Um token é válido se: 1. O username do token bate com o username do
	 * UserDetails 2. O token não está expirado
	 * 
	 * @param token       token JWT
	 * @param userDetails detalhes do usuário
	 * @return true se o token é válido
	 */
	public boolean isTokenValid(final String token, final UserDetails userDetails) {
		final String extractedUsername = extractUsername(token);
		return extractedUsername.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	/**
	 * Verifica se o token está expirado.
	 * 
	 * @param token token JWT
	 * @return true se o token está expirado
	 */
	private boolean isTokenExpired(final String token) {
		final Date expiration = extractClaim(token, Claims::getExpiration);
		return expiration.before(new Date());
	}

	/**
	 * Gera a chave de assinatura usando o secret configurado.
	 * 
	 * @return chave para assinar o token
	 */
	private Key getSignKey() {
		return Keys.hmacShaKeyFor(this.secretKey.getBytes());
	}
}