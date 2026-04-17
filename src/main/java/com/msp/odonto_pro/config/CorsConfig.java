package com.msp.odonto_pro.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOriginPatterns(List.of("http://localhost:5173", "http://127.0.0.1:5173",
				"http://localhost:3000", "http://127.0.0.1:3000"));

		// Métodos HTTP permitidos
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

		// Headers permitidos
		config.setAllowedHeaders(List.of("*"));

		// Headers expostos (para o frontend acessar)
		config.setExposedHeaders(List.of("Authorization", "Content-Type"));

		// Permitir envio de credenciais (cookies, tokens)
		config.setAllowCredentials(true);

		// Cache da preflight request (em segundos)
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}