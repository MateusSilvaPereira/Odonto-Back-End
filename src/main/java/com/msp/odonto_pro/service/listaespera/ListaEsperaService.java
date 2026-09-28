package com.msp.odonto_pro.service.listaespera;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.listaespera.ListaEsperaDTO;
import com.msp.odonto_pro.entity.dentista.Dentista;
import com.msp.odonto_pro.entity.listaespera.ListaEspera;
import com.msp.odonto_pro.entity.paciente.Paciente;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repository.dentista.DentistaRepository;
import com.msp.odonto_pro.repository.listaespera.ListaEsperaRepository;
import com.msp.odonto_pro.repository.paciente.PacienteRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListaEsperaService {

	private final ListaEsperaRepository listaEsperaRepository;
	private final PacienteRepository pacienteRepository;
	private final DentistaRepository dentistaRepository;

	public List<ListaEsperaDTO> listar() {
		return this.listaEsperaRepository.listarTodosComPacienteEDentista(SecurityUtils.clinicaIdAtual()).stream()
				.map(this::toDto).collect(Collectors.toList());
	}

	public ListaEsperaDTO criar(final ListaEsperaDTO dto) {
		if (dto.getPacienteId() == null) {
			throw new IllegalArgumentException("pacienteId é obrigatório");
		}
		if (dto.getProcedimento() == null || dto.getProcedimento().isBlank()) {
			throw new IllegalArgumentException("procedimento é obrigatório");
		}

		final Paciente paciente = this.exigirPaciente(dto.getPacienteId());
		final Dentista dentista = this.resolverDentista(dto.getDentistaId());

		final ListaEspera item = ListaEspera.builder().paciente(paciente).procedimento(dto.getProcedimento())
				.dentista(dentista).observacao(dto.getObservacao()).status("aguardando").build();

		return this.toDto(this.listaEsperaRepository.save(item));
	}

	public ListaEsperaDTO marcarAgendado(final Long id) {
		final ListaEspera item = this.exigir(id);
		item.setStatus("agendado");
		return this.toDto(this.listaEsperaRepository.save(item));
	}

	public void excluir(final Long id) {
		this.exigir(id);
		this.listaEsperaRepository.deleteById(id);
	}

	private ListaEspera exigir(final Long id) {
		final ListaEspera item = this.listaEsperaRepository.findById(id).orElseThrow(
				() -> new NotFoundException("Item da lista de espera com id: " + id + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(item.getPaciente().getClinicaId())) {
			throw new NotFoundException("Item da lista de espera com id: " + id + " não encontrado!");
		}
		return item;
	}

	private Paciente exigirPaciente(final Long pacienteId) {
		final Paciente paciente = this.pacienteRepository.findById(pacienteId)
				.orElseThrow(() -> new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(paciente.getClinicaId())) {
			throw new NotFoundException("Paciente com id: " + pacienteId + " não encontrado!");
		}
		return paciente;
	}

	private Dentista resolverDentista(final Long dentistaId) {
		if (dentistaId == null) {
			return null;
		}
		final Dentista dentista = this.dentistaRepository.findById(dentistaId)
				.orElseThrow(() -> new NotFoundException("Dentista com id: " + dentistaId + " não encontrado!"));
		if (!SecurityUtils.clinicaIdAtual().equals(dentista.getClinicaId())) {
			throw new NotFoundException("Dentista com id: " + dentistaId + " não encontrado!");
		}
		return dentista;
	}

	private ListaEsperaDTO toDto(final ListaEspera item) {
		return ListaEsperaDTO.builder().id(item.getId()).pacienteId(item.getPaciente().getId())
				.nomePaciente(item.getPaciente().getNome()).procedimento(item.getProcedimento())
				.dentistaId(item.getDentista() != null ? item.getDentista().getId() : null)
				.dentista(item.getDentista() != null ? item.getDentista().getNome() : null)
				.observacao(item.getObservacao()).status(item.getStatus()).criadoEm(item.getCriadoEm()).build();
	}
}
