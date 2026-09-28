package com.msp.odonto_pro.controller.paciente;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msp.odonto_pro.dto.paciente.PacienteDTO;
import com.msp.odonto_pro.dto.paciente.PacienteProjection;
import com.msp.odonto_pro.security.JwtService;
import com.msp.odonto_pro.security.UserDetailsServiceImp;
import com.msp.odonto_pro.service.paciente.PacienteService;

/**
 * Rede de segurança do contrato de paciente: {@code /api/v1/pacientes},
 * criação com 201 e exclusão com 204.
 */
@WebMvcTest(controllers = PacienteController.class)
@AutoConfigureMockMvc(addFilters = false)
class PacienteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean
	private PacienteService pacienteService;

	// JwtAuthFilter é um @Component (bean de Filter), por isso é instanciado mesmo
	// com addFilters=false — precisa das dependências dele mockadas para o contexto subir.
	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UserDetailsServiceImp userDetailsServiceImp;

	@Test
	@WithMockUser(authorities = "write:paciente")
	void criarPacienteRetorna201() throws Exception {
		final PacienteDTO salvo = PacienteDTO.builder().id(1L).nome("Maria Silva").build();
		given(this.pacienteService.save(any())).willReturn(salvo);

		final PacienteDTO request = PacienteDTO.builder().nome("Maria Silva").build();

		this.mockMvc
				.perform(post("/api/v1/pacientes").contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.nome").value("Maria Silva"));
	}

	@Test
	@WithMockUser(authorities = "read:paciente")
	void listarPacientesRetorna200() throws Exception {
		given(this.pacienteService.listarPacientesPaginado(anyInt(), anyInt())).willReturn(Page.<PacienteProjection>empty());

		this.mockMvc.perform(get("/api/v1/pacientes")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = "delete:paciente")
	void excluirPacienteRetorna204() throws Exception {
		this.mockMvc.perform(delete("/api/v1/pacientes/1")).andExpect(status().isNoContent());
	}
}
