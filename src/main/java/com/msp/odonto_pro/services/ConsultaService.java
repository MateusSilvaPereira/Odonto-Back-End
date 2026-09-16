package com.msp.odonto_pro.services;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dtos.ConsultaDTO;
import com.msp.odonto_pro.entity.Consulta;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.projections.consultas.ConsultaResumoProjection;
import com.msp.odonto_pro.repositorys.ConsultaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultaService {

	private final ConsultaRepository consultaRepository;
	private final ModelMapper mapper;

	public ConsultaDTO save(ConsultaDTO consultaDTO) {
		return mapper.map(consultaRepository.save(mapper.map(consultaDTO, Consulta.class)), ConsultaDTO.class);
	}

	public List<ConsultaResumoProjection> listarResumo() {
		return this.consultaRepository.listarResumo();
	}

	public void excluir(Long id) {
		if (!this.consultaRepository.existsById(id)) {
			throw new NotFoundException("Consulta com id: " + id + " não encontrado!");
		}

		this.consultaRepository.deleteById(id);
	}

}
