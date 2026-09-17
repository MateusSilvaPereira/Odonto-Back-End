package com.msp.odonto_pro.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.msp.odonto_pro.exceptions.AccessDeniedHandlerImpl;
import com.msp.odonto_pro.exceptions.AuthenticationEntryPointImpl;

/**
 * Configuração principal de segurança da aplicação.
 * 
 * Responsabilidades: 1. Definir quais URLs são públicas e quais requerem
 * autenticação 2. Configurar regras de acesso por Role (URL-based security) 3.
 * Habilitar @PreAuthorize para segurança granular (method-based security) 4.
 * Configurar CORS, CSRF, sessões 5. Registrar o JwtAuthFilter na cadeia de
 * filtros
 * 
 * IMPORTANTE: @EnableMethodSecurity habilita o uso de @PreAuthorize nos
 * controllers.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 🔑 HABILITA @PreAuthorize NOS CONTROLLERS
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final CorsConfigurationSource corsConfigurationSource;
	private final AccessDeniedHandlerImpl accessDeniedHandler;
	private final AuthenticationEntryPointImpl authenticationEntryPoint;

	public SecurityConfig(final JwtAuthFilter jwtAuthFilter, final CorsConfigurationSource corsConfigurationSource) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.corsConfigurationSource = corsConfigurationSource;
		this.accessDeniedHandler = new AccessDeniedHandlerImpl();
		this.authenticationEntryPoint = new AuthenticationEntryPointImpl();
	}

	/**
	 * Encoder de senhas usando BCrypt.
	 * 
	 * BCrypt é o padrão ouro para hashing de senhas porque: - Adiciona salt
	 * automaticamente - É lento por design (resistente a ataques de força bruta) -
	 * O custo pode ser ajustado conforme o hardware evolui
	 * 
	 * @return encoder de senhas
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Gerenciador de autenticação do Spring Security.
	 * 
	 * @param config configuração de autenticação
	 * @return AuthenticationManager
	 * @throws Exception se houver erro na configuração
	 */
	@Bean
	AuthenticationManager authenticationManager(final AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	/**
	 * Cadeia de filtros de segurança.
	 * 
	 * Ordem das configurações: 1. CORS (Cross-Origin Resource Sharing) 2. CSRF
	 * (desabilitado porque usamos JWT, não cookies) 3. Headers de segurança 4.
	 * Regras de autorização por URL 5. Política de sessão (STATELESS porque usamos
	 * JWT) 6. Filtro JWT (antes do UsernamePasswordAuthenticationFilter)
	 * 
	 * @param http configuração HTTP do Spring Security
	 * @return cadeia de filtros construída
	 * @throws Exception se houver erro na configuração
	 */
	@Bean
	SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
		http
				// ===== CORS =====
				.cors(cors -> cors.configurationSource(this.corsConfigurationSource))

				// ===== CSRF =====
				// Desabilitado porque usamos JWT (não usamos cookies de sessão)
				.csrf(csrf -> csrf.disable())

				// ===== HEADERS DE SEGURANÇA =====
				.headers(headers -> headers.frameOptions(frame -> frame.disable()) // Permite iframes (Swagger)
						.xssProtection(xss -> xss.disable()).contentTypeOptions(configurer -> configurer.disable()))

				// ===== AUTORIZAÇÃO POR URL =====
				.authorizeHttpRequests(auth -> auth
						// --- URLs PÚBLICAS ---
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs",
								"/v3/api-docs/swagger-config", "/swagger-resources/**", "/webjars/**", "/auth/**",
								"/uploads/**")
						.permitAll()

						// --- USUÁRIOS (só ADMIN) ---
						.requestMatchers("/usuario/**").hasRole("ADMIN")

						// --- NOTIFICAÇÕES / CONFIG DE LEMBRETES (só ADMIN) ---
						.requestMatchers("/notificacoes/**").hasRole("ADMIN")

						// --- FINANCEIRO (só ADMIN) ---
						.requestMatchers("/financeiro/**").hasRole("ADMIN")
						.requestMatchers("/pagamentos/**").hasRole("ADMIN")

						// --- RELATÓRIO GERAL CONSOLIDADO (só ADMIN) ---
						.requestMatchers("/relatorios/**").hasRole("ADMIN")

						// --- DENTISTAS (ADMIN ou DENTISTA) ---
						.requestMatchers(HttpMethod.DELETE, "/dentista/**").hasRole("ADMIN")
						.requestMatchers("/dentista/**").hasAnyRole("ADMIN", "DENTISTA")

						// --- TRATAMENTOS (ADMIN ou DENTISTA) ---
						.requestMatchers(HttpMethod.DELETE, "/tratamento/**").hasRole("ADMIN")
						.requestMatchers("/tratamento/**").hasAnyRole("ADMIN", "DENTISTA")

						// --- PACIENTES (todos os roles autenticados) ---
						.requestMatchers(HttpMethod.DELETE, "/paciente/**").hasRole("ADMIN")
						.requestMatchers("/paciente/**").hasAnyRole("ADMIN", "DENTISTA", "RECEPCIONISTA")

						// --- ODONTOGRAMA E PLANO DE TRATAMENTO (sub-recursos de paciente) ---
						.requestMatchers("/pacientes/**").hasAnyRole("ADMIN", "DENTISTA", "RECEPCIONISTA")
						.requestMatchers(HttpMethod.DELETE, "/tratamentos/**").hasRole("ADMIN")
						.requestMatchers("/tratamentos/**").hasAnyRole("ADMIN", "DENTISTA", "RECEPCIONISTA")

						// --- CONSULTAS (ADMIN, DENTISTA ou RECEPCIONISTA) ---
						.requestMatchers(HttpMethod.DELETE, "/consultas/**").hasRole("ADMIN")
						.requestMatchers("/consultas/**").hasAnyRole("ADMIN", "DENTISTA", "RECEPCIONISTA")

						// --- QUALQUER OUTRA URL REQUER AUTENTICAÇÃO ---
						.anyRequest().authenticated())

				// ===== SESSÃO =====
				// STATELESS = não cria sessão HTTP (usamos JWT)
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.accessDeniedHandler(this.accessDeniedHandler) // 403
						.authenticationEntryPoint(this.authenticationEntryPoint) // 401
				)

				// ===== FILTRO JWT =====
				// Adiciona nosso filtro ANTES do filtro padrão do Spring
				.addFilterBefore(this.jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}