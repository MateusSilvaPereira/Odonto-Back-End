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
import com.msp.odonto_pro.web.ApiPaths;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize nos controllers, além das regras de URL abaixo
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

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(final AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

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
						// --- Exceção dentro de /auth/** (avaliada antes do permitAll abaixo,
						// já que a 1ª regra que casar vence) ---
						.requestMatchers(ApiPaths.V1 + "/auth/alterar-senha").authenticated()

						// --- URLs PÚBLICAS. Uploads, Swagger e o health check ficam fora do
						// versionamento de propósito (não são endpoints de API de negócio) ---
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs",
								"/v3/api-docs/swagger-config", "/swagger-resources/**", "/webjars/**", "/uploads/**",
								"/actuator/health")
						.permitAll()
						.requestMatchers(ApiPaths.V1 + "/auth/**", ApiPaths.V1 + "/public/**").permitAll()

						// --- USUÁRIOS (ADMIN; exceções: redefinição de senha também SUPER_ADMIN,
						// e /me é o próprio usuário autenticado editando o próprio perfil) ---
						.requestMatchers(ApiPaths.V1 + "/usuarios/*/enviar-redefinicao-senha")
						.hasAnyRole("ADMIN", "SUPER_ADMIN")
						.requestMatchers(HttpMethod.PUT, ApiPaths.V1 + "/usuarios/me").authenticated()
						.requestMatchers(ApiPaths.V1 + "/usuarios/**").hasRole("ADMIN")

						// --- CLÍNICAS (multi-tenant): CRUD por id só SUPER_ADMIN; /me também ADMIN
						// (checagem fina de qual endpoint é feita via @PreAuthorize no controller) ---
						.requestMatchers(ApiPaths.V1 + "/clinicas/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

						// --- CONFIGURAÇÃO DA CLÍNICA (legado, só ADMIN) ---
						.requestMatchers(ApiPaths.V1 + "/configuracao-clinica/**").hasRole("ADMIN")

						// --- NOTIFICAÇÕES / CONFIG DE LEMBRETES (só ADMIN) ---
						.requestMatchers(ApiPaths.V1 + "/notificacoes/**").hasRole("ADMIN")

						// --- RECALL (campanha de reativação; só ADMIN) ---
						.requestMatchers(ApiPaths.V1 + "/recall/**").hasRole("ADMIN")

						// --- AUDITORIA LGPD (só ADMIN) ---
						.requestMatchers(ApiPaths.V1 + "/auditoria/**").hasRole("ADMIN")

						// --- FINANCEIRO (ADMIN ou RECEPCIONISTA) ---
						.requestMatchers(ApiPaths.V1 + "/financeiro/**").hasAnyRole("ADMIN", "RECEPCIONISTA")
						.requestMatchers(ApiPaths.V1 + "/pagamentos/**").hasAnyRole("ADMIN", "RECEPCIONISTA")
						.requestMatchers(ApiPaths.V1 + "/parcelas/**").hasAnyRole("ADMIN", "RECEPCIONISTA")
						.requestMatchers(ApiPaths.V1 + "/cobrancas/**").hasAnyRole("ADMIN", "RECEPCIONISTA")

						// --- RELATÓRIO GERAL CONSOLIDADO (só ADMIN) ---
						.requestMatchers(ApiPaths.V1 + "/relatorios/**").hasRole("ADMIN")

						// --- ESTOQUE (ADMIN, DENTISTA ou RECEPCIONISTA; exclusão só ADMIN) ---
						.requestMatchers(HttpMethod.DELETE, ApiPaths.V1 + "/estoque/**").hasRole("ADMIN")
						.requestMatchers(ApiPaths.V1 + "/estoque/**").hasAnyRole("ADMIN", "DENTISTA", "RECEPCIONISTA")

						// --- DENTISTAS (ADMIN ou DENTISTA) ---
						.requestMatchers(HttpMethod.DELETE, ApiPaths.V1 + "/dentistas/**").hasRole("ADMIN")
						.requestMatchers(ApiPaths.V1 + "/dentistas/**").hasAnyRole("ADMIN", "DENTISTA",
								"RECEPCIONISTA")

						// --- BLOQUEIOS DE AGENDA (sub-recurso de dentista) ---
						.requestMatchers(ApiPaths.V1 + "/bloqueios-agenda/**").hasAnyRole("ADMIN", "DENTISTA",
								"RECEPCIONISTA")

						// --- PACIENTES (todos os roles autenticados) ---
						// Exceção: excluir anexo clínico é permitido a quem pode escrever no
						// prontuário (ADMIN/DENTISTA/RECEPCIONISTA), não só ADMIN — avaliada
						// antes da regra genérica de DELETE de paciente, que é mais restritiva.
						.requestMatchers(HttpMethod.DELETE, ApiPaths.V1 + "/pacientes/*/anexos/**")
						.hasAnyRole("ADMIN", "DENTISTA", "RECEPCIONISTA")
						.requestMatchers(HttpMethod.DELETE, ApiPaths.V1 + "/pacientes/**").hasRole("ADMIN")
						.requestMatchers(ApiPaths.V1 + "/pacientes/**").hasAnyRole("ADMIN", "DENTISTA",
								"RECEPCIONISTA")

						// --- CONVÊNIOS (sub-cadastro de paciente) ---
						.requestMatchers(HttpMethod.DELETE, ApiPaths.V1 + "/convenios/**").hasRole("ADMIN")
						.requestMatchers(ApiPaths.V1 + "/convenios/**").hasAnyRole("ADMIN", "DENTISTA",
								"RECEPCIONISTA")

						// --- PLANO DE TRATAMENTO (sub-recurso de paciente) ---
						.requestMatchers(HttpMethod.DELETE, ApiPaths.V1 + "/tratamentos/**").hasRole("ADMIN")
						.requestMatchers(ApiPaths.V1 + "/tratamentos/**").hasAnyRole("ADMIN", "DENTISTA",
								"RECEPCIONISTA")
						.requestMatchers(HttpMethod.DELETE, ApiPaths.V1 + "/orcamentos/**").hasRole("ADMIN")
						.requestMatchers(ApiPaths.V1 + "/orcamentos/**").hasAnyRole("ADMIN", "DENTISTA",
								"RECEPCIONISTA")

						// --- CONSULTAS (ADMIN, DENTISTA ou RECEPCIONISTA) ---
						.requestMatchers(HttpMethod.DELETE, ApiPaths.V1 + "/consultas/**").hasRole("ADMIN")
						.requestMatchers(ApiPaths.V1 + "/consultas/**").hasAnyRole("ADMIN", "DENTISTA",
								"RECEPCIONISTA")

						// --- LISTA DE ESPERA (parte operacional da agenda) ---
						.requestMatchers(ApiPaths.V1 + "/lista-espera/**").hasAnyRole("ADMIN", "DENTISTA",
								"RECEPCIONISTA")

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