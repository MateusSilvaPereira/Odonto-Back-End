package com.msp.odonto_pro.service.evolucaoclinica;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.evolucaoclinica.EvolucaoClinicaDTO;
import com.msp.odonto_pro.entity.evolucaoclinica.EvolucaoClinica;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.consulta.ConsultaRepository;
import com.msp.odonto_pro.repository.evolucaoclinica.EvolucaoClinicaRepository;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

/**
 * Timeline clínica do paciente. Append-only por design — não existe
 * atualizar/excluir aqui de propósito (ver EvolucaoClinica).
 */
@Service
@RequiredArgsConstructor
public class EvolucaoClinicaService {

	private final EvolucaoClinicaRepository evolucaoClinicaRepository;
	private final PacienteRepository pacienteRepository;
	private final ConsultaRepository consultaRepository;

	public List<EvolucaoClinicaDTO> listar(final Long pacienteId) {
		this.exigirPaciente(pacienteId);
		return this.evolucaoClinicaRepository.findByPacienteIdOrderByDataAscCriadoEmAsc(pacienteId).stream()
				.map(this::toDto).collect(Collectors.toList());
	}

	public EvolucaoClinicaDTO criar(final Long pacienteId, final EvolucaoClinicaDTO dto, final Usuario usuarioLogado) {
		final Paciente paciente = this.exigirPaciente(pacienteId);

		if (dto.getData() == null) {
			throw new IllegalArgumentException("data é obrigatória");
		}
		if (dto.getEvolucao() == null || dto.getEvolucao().isBlank()) {
			throw new IllegalArgumentException("evolucao é obrigatória");
		}

		if (dto.getRetificaEvolucaoId() != null
				&& !this.evolucaoClinicaRepository.existsByIdAndPacienteId(dto.getRetificaEvolucaoId(), pacienteId)) {
			throw new NotFoundException(
					"Evolução a retificar (id: " + dto.getRetificaEvolucaoId() + ") não encontrada para este paciente");
		}
		if (dto.getConsultaId() != null
				&& !this.consultaRepository.existsByIdAndPacienteId(dto.getConsultaId(), pacienteId)) {
			throw new NotFoundException(
					"Consulta com id: " + dto.getConsultaId() + " não encontrada para este paciente");
		}

		final EvolucaoClinica evolucao = new EvolucaoClinica();
		evolucao.setPaciente(paciente);
		evolucao.setData(dto.getData());
		evolucao.setDentista(dto.getDentista());
		evolucao.setQueixaPrincipal(dto.getQueixaPrincipal());
		evolucao.setDiagnostico(dto.getDiagnostico());
		evolucao.setProcedimentoRealizado(dto.getProcedimentoRealizado());
		evolucao.setEvolucao(dto.getEvolucao());
		evolucao.setPrescricao(dto.getPrescricao());
		evolucao.setCriadoPor(usuarioLogado);
		evolucao.setRetificaEvolucaoId(dto.getRetificaEvolucaoId());
		evolucao.setConsultaId(dto.getConsultaId());

		return this.toDto(this.evolucaoClinicaRepository.save(evolucao));
	}

	private Paciente exigirPaciente(final Long pacienteId) {
		final Paciente paciente = this.pacienteRepository.findById(pacienteId)
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(paciente.getClinicaId())) {
			throw new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!");
		}
		return paciente;
	}

	private EvolucaoClinicaDTO toDto(final EvolucaoClinica evolucao) {
		return EvolucaoClinicaDTO.builder().id(evolucao.getId()).pacienteId(evolucao.getPaciente().getId())
				.data(evolucao.getData()).dentista(evolucao.getDentista())
				.queixaPrincipal(evolucao.getQueixaPrincipal()).diagnostico(evolucao.getDiagnostico())
				.procedimentoRealizado(evolucao.getProcedimentoRealizado()).evolucao(evolucao.getEvolucao())
				.prescricao(evolucao.getPrescricao()).criadoEm(evolucao.getCriadoEm())
				.criadoPor(evolucao.getCriadoPor() != null ? evolucao.getCriadoPor().getNome() : null)
				.retificaEvolucaoId(evolucao.getRetificaEvolucaoId()).consultaId(evolucao.getConsultaId()).build();
	}
}
