package com.msp.odonto_pro.controller.consulta;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.msp.odonto_pro.dto.consulta.ConsultaDTO;
import com.msp.odonto_pro.dto.consulta.StatusRequest;
import com.msp.odonto_pro.security.JwtService;
import com.msp.odonto_pro.security.UserDetailsServiceImp;
import com.msp.odonto_pro.service.consulta.ConsultaService;

/**
 * Rede de segurança do contrato de consulta: {@code /api/v1/consultas},
 * criação com 201 e mudança de status/reagendamento via PATCH.
 */
@WebMvcTest(controllers = ConsultaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConsultaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	@MockitoBean
	private ConsultaService consultaService;

	// JwtAuthFilter é um @Component (bean de Filter), por isso é instanciado mesmo
	// com addFilters=false — precisa das dependências dele mockadas para o contexto subir.
	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UserDetailsServiceImp userDetailsServiceImp;

	@Test
	@WithMockUser(authorities = "write:consulta")
	void criarConsultaRetorna201() throws Exception {
		final ConsultaDTO salva = ConsultaDTO.builder().id(1L).pacienteId(10L).status("pendente").build();
		given(this.consultaService.criar(any())).willReturn(salva);

		final ConsultaDTO request = ConsultaDTO.builder().pacienteId(10L).data(LocalDate.now().plusDays(1))
				.horario(LocalTime.of(10, 0)).build();

		this.mockMvc
				.perform(post("/api/v1/consultas").contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	@WithMockUser(authorities = "read:consulta")
	void listarConsultasRetorna200() throws Exception {
		given(this.consultaService.listar()).willReturn(List.of());

		this.mockMvc.perform(get("/api/v1/consultas")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = "write:consulta")
	void atualizarStatusViaPatchRetorna200() throws Exception {
		final ConsultaDTO atualizada = ConsultaDTO.builder().id(1L).status("confirmado").build();
		given(this.consultaService.atualizarStatus(anyLong(), anyString())).willReturn(atualizada);

		final StatusRequest request = new StatusRequest();
		request.setStatus("confirmado");

		this.mockMvc
				.perform(patch("/api/v1/consultas/1/status").contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("confirmado"));
	}
}
