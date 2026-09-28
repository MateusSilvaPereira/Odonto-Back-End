package com.msp.odonto_pro.service.prontuario;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.prontuario.ProntuarioDTO;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.entity.prontuario.Prontuario;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;
import com.msp.odonto_pro.repository.prontuario.ProntuarioRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProntuarioService {

	private final ProntuarioRepository prontuarioRepository;
	private final PacienteRepository pacienteRepository;

	public ProntuarioDTO obter(final Long pacienteId) {
		this.exigirPaciente(pacienteId);
		return this.prontuarioRepository.findByPacienteId(pacienteId).map(this::toDto)
				.orElseGet(() -> ProntuarioDTO.builder().build());
	}

	public ProntuarioDTO atualizar(final Long pacienteId, final ProntuarioDTO dto, final Usuario usuarioLogado) {
		final Paciente paciente = this.exigirPaciente(pacienteId);

		final Prontuario prontuario = this.prontuarioRepository.findByPacienteId(pacienteId).orElseGet(() -> {
			final Prontuario novo = new Prontuario();
			novo.setPaciente(paciente);
			return novo;
		});

		prontuario.setAnamnese(dto.getAnamnese());
		prontuario.setHistoricoMedico(dto.getHistoricoMedico());
		prontuario.setAlergias(dto.getAlergias());
		prontuario.setMedicamentosUso(dto.getMedicamentosUso());
		prontuario.setAtualizadoPor(usuarioLogado);

		return this.toDto(this.prontuarioRepository.save(prontuario));
	}

	private Paciente exigirPaciente(final Long pacienteId) {
		final Paciente paciente = this.pacienteRepository.findById(pacienteId)
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(paciente.getClinicaId())) {
			throw new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!");
		}
		return paciente;
	}

	private ProntuarioDTO toDto(final Prontuario prontuario) {
		return ProntuarioDTO.builder().anamnese(prontuario.getAnamnese())
				.historicoMedico(prontuario.getHistoricoMedico()).alergias(prontuario.getAlergias())
				.medicamentosUso(prontuario.getMedicamentosUso()).atualizadoEm(prontuario.getAtualizadoEm())
				.atualizadoPor(prontuario.getAtualizadoPor() != null ? prontuario.getAtualizadoPor().getNome() : null)
				.build();
	}
}
