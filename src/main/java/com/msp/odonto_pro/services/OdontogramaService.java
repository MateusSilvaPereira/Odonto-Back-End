package com.msp.odonto_pro.services;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dtos.DenteRegistroDTO;
import com.msp.odonto_pro.entity.OdontogramaDente;
import com.msp.odonto_pro.entity.Paciente;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repositorys.OdontogramaDenteRepository;
import com.msp.odonto_pro.repositorys.PacienteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OdontogramaService {

	private static final Set<String> STATUS_VALIDOS = Set.of("higido", "cariado", "restaurado", "tratamento_canal",
			"extraido", "protese", "implante");

	private final OdontogramaDenteRepository odontogramaDenteRepository;
	private final PacienteRepository pacienteRepository;

	public List<DenteRegistroDTO> listar(final Long pacienteId) {
		this.exigirPaciente(pacienteId);
		return this.odontogramaDenteRepository.findByPacienteId(pacienteId).stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	public DenteRegistroDTO atualizarDente(final Long pacienteId, final String numero, final DenteRegistroDTO dto) {
		final Paciente paciente = this.exigirPaciente(pacienteId);
		this.validarStatus(dto.getStatus());

		final OdontogramaDente registro = this.odontogramaDenteRepository
				.findByPacienteIdAndNumero(pacienteId, numero).orElseGet(() -> {
					final OdontogramaDente novo = new OdontogramaDente();
					novo.setPaciente(paciente);
					novo.setNumero(numero);
					return novo;
				});

		registro.setStatus(dto.getStatus());
		registro.setObservacao(dto.getObservacao());

		return this.toDto(this.odontogramaDenteRepository.save(registro));
	}

	private Paciente exigirPaciente(final Long pacienteId) {
		return this.pacienteRepository.findById(pacienteId)
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!"));
	}

	private void validarStatus(final String status) {
		if (status == null || !STATUS_VALIDOS.contains(status)) {
			throw new IllegalArgumentException(
					"Status inválido. Use: higido, cariado, restaurado, tratamento_canal, extraido, protese ou implante");
		}
	}

	private DenteRegistroDTO toDto(final OdontogramaDente registro) {
		return DenteRegistroDTO.builder().numero(registro.getNumero()).status(registro.getStatus())
				.observacao(registro.getObservacao()).atualizadoEm(registro.getAtualizadoEm()).build();
	}
}
