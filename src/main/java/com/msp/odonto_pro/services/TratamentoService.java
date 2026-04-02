package com.msp.odonto_pro.services;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dtos.TratamentoDTO;
import com.msp.odonto_pro.entity.Tratamento;
import com.msp.odonto_pro.repositorys.TratamentoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TratamentoService {

	private final TratamentoRepository tratamentoRepository;
	private final ModelMapper mapper;

	// Salvar tratamento...
	public TratamentoDTO save(TratamentoDTO dto) {
		return mapper.map(tratamentoRepository.save(mapper.map(dto, Tratamento.class)), TratamentoDTO.class);
	}
}
