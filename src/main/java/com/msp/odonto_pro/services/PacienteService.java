package com.msp.odonto_pro.services;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dtos.PacienteDTO;
import com.msp.odonto_pro.entity.Paciente;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.projections.pacientes.PacienteProjection;
import com.msp.odonto_pro.repositorys.PacienteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PacienteService {

	private final PacienteRepository pacienteRepository;
	private final ModelMapper mapper;

	// Salvar paciente...
	public PacienteDTO save(PacienteDTO dto) {
		return mapper.map(pacienteRepository.save(mapper.map(dto, Paciente.class)), PacienteDTO.class);
	}

	// Deletar Paciente...
	public void excluir(Long id) {
		if (!this.pacienteRepository.existsById(id)) {
			throw new NotFoundException("Paciente com id: " + id + " não encontrado!");
		}

		this.pacienteRepository.deleteById(id);
	}

	// Listar pacientes
	public Page<PacienteProjection> listarPacientesPaginado(Integer pagina, Integer quantidade) {
		Sort sort = Sort.by("nome").ascending();
		PageRequest pageRequest = PageRequest.of(pagina - 1, quantidade, sort);
		return pacienteRepository.listarPacientes(pageRequest);
	}

}
