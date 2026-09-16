package com.msp.odonto_pro.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msp.odonto_pro.dtos.DentistaDTO;
import com.msp.odonto_pro.services.DentistaService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/dentista")
@RequiredArgsConstructor
public class DentistaController {

	private final DentistaService dentistaService;

	@PreAuthorize("hasAuthority('write:dentista')")
	@Operation(summary = "Salvar dentista.")
	@PostMapping("/salvar")
	public ResponseEntity<DentistaDTO> salvar(@RequestBody final DentistaDTO dentista) {
		return ResponseEntity.ok(this.dentistaService.save(dentista));
	}

	@PreAuthorize("hasAuthority('delete:dentista')")
	@Operation(description = "Excluir Dentista por id")
	@DeleteMapping(value = "/deletar/{id}")
	public ResponseEntity<Void> deletarById(@PathVariable final Long id) {
		this.dentistaService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}