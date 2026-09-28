package com.msp.odonto_pro.controller.usuario;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msp.odonto_pro.dto.usuario.LoginRequest;
import com.msp.odonto_pro.dto.usuario.LoginResponse;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.enums.Role;
import com.msp.odonto_pro.security.JwtService;
import com.msp.odonto_pro.security.UserDetailsServiceImp;
import com.msp.odonto_pro.service.usuario.AuthService;
import com.msp.odonto_pro.service.usuario.PasswordResetService;

/**
 * Rede de segurança para o contrato final de login: {@code POST /api/v1/auth/login}.
 * Ainda falha hoje porque a rota vigente é {@code /auth/login} — passa a verde
 * quando a Fase 4 (versionamento /api/v1) do plano de refatoração for aplicada.
 */
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private PasswordResetService passwordResetService;

	// JwtAuthFilter é um @Component (bean de Filter), por isso é instanciado mesmo
	// com addFilters=false — precisa das dependências dele mockadas para o contexto subir.
	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UserDetailsServiceImp userDetailsServiceImp;

	@Test
	void loginComCredenciaisValidasRetornaTokenEDadosDoUsuario() throws Exception {
		final Usuario usuario = new Usuario();
		usuario.setId(1L);
		usuario.setNome("Dra. Ana");
		usuario.setUsername("ana");
		usuario.setEmail("ana@example.com");
		usuario.setRole(Role.ADMIN);
		usuario.setAtivo(true);

		given(this.authService.login(any())).willReturn(new LoginResponse("jwt-token-fake", usuario, "Clínica Central"));

		final LoginRequest request = new LoginRequest("ana", "senha-correta");

		this.mockMvc
				.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("jwt-token-fake"))
				.andExpect(jsonPath("$.user.username").value("ana"));
	}

	@Test
	void loginComCredenciaisInvalidasRetorna401() throws Exception {
		given(this.authService.login(any())).willThrow(new BadCredentialsException("Usuário ou senha inválidos"));

		final LoginRequest request = new LoginRequest("ana", "senha-errada");

		this.mockMvc
				.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());
	}
}
