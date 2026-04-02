package com.msp.odonto_pro.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msp.odonto_pro.dtos.TratamentoDTO;
import com.msp.odonto_pro.services.TratamentoService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin
@RequestMapping(value = "/tratamento")
@RequiredArgsConstructor
public class TratamentoController {

	private final TratamentoService tratamentoService;

	@Operation(summary = "Salvar tratamento.")
	@PostMapping("/salvar")
	public ResponseEntity<TratamentoDTO> salvar(@RequestBody TratamentoDTO tratamentoDTO) {
		return ResponseEntity.ok(this.tratamentoService.save(tratamentoDTO));
	}
}