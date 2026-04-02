package com.msp.odonto_pro.services;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dtos.DentistaDTO;
import com.msp.odonto_pro.entity.Dentista;
import com.msp.odonto_pro.exceptions.NotFoundException;
import com.msp.odonto_pro.repositorys.DentistaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DentistaService {

	private final DentistaRepository dentistaRepository;
	private final ModelMapper mapper;

	// Salvar dentista...
	public DentistaDTO save(DentistaDTO dto) {
		return mapper.map(dentistaRepository.save(mapper.map(dto, Dentista.class)), DentistaDTO.class);
	}
	
	// Deletar Dentista...
	public void excluir(Long id) {
		if(!this.dentistaRepository.existsById(id)) {
			throw new NotFoundException("Dentista com id: " + id + " não encontrado!");
		}
		this.dentistaRepository.deleteById(id);
	}
}
