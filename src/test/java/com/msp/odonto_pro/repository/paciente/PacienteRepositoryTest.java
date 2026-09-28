package com.msp.odonto_pro.repository.paciente;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.dto.paciente.PacienteProjection;

/**
 * Valida a query nativa usada pela listagem paginada de pacientes
 * (isolamento por clínica), a base da rota {@code GET /api/v1/pacientes}.
 */
@DataJpaTest
class PacienteRepositoryTest {

	@Autowired
	private PacienteRepository pacienteRepository;

	@Test
	void listarPacientesRetornaSomenteOsDaClinicaInformada() {
		final Paciente daClinica1 = Paciente.builder().clinicaId(1L).nome("Paciente Clinica 1").build();
		final Paciente deOutraClinica = Paciente.builder().clinicaId(2L).nome("Paciente Clinica 2").build();
		this.pacienteRepository.save(daClinica1);
		this.pacienteRepository.save(deOutraClinica);

		final Page<PacienteProjection> pagina = this.pacienteRepository.listarPacientes(1L, PageRequest.of(0, 10));

		assertThat(pagina.getTotalElements()).isEqualTo(1);
		assertThat(pagina.getContent().get(0).getNome()).isEqualTo("Paciente Clinica 1");
	}
}
