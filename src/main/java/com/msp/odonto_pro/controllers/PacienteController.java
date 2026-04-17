package com.msp.odonto_pro.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msp.odonto_pro.dtos.PacienteDTO;
import com.msp.odonto_pro.projections.pacientes.PacienteProjection;
import com.msp.odonto_pro.services.PacienteService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/paciente")
@RequiredArgsConstructor
public class PacienteController {

	private final PacienteService pacienteService;

	// Salvar paciente...
	@Operation(summary = "Salvar paciente.")
	@PostMapping("/salvar")
	public ResponseEntity<PacienteDTO> salvar(@RequestBody PacienteDTO pacienteDTO) {
		return ResponseEntity.ok(this.pacienteService.save(pacienteDTO));
	}

	// Deletar paciente por id...
	@Operation(description = "Deletar Paciente por id.")
	@DeleteMapping(value = "/delete/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		this.pacienteService.excluir(id);
		return ResponseEntity.ok().build();
	}

	// Listar pacientes
	@Operation(summary = "Listar pacientes paginado")
	@GetMapping("/listar")
	public ResponseEntity<Page<PacienteProjection>> listar(
			@RequestParam(value = "p", required = false, defaultValue = "1") Integer pagina,
			@RequestParam(value = "q", required = false, defaultValue = "50") Integer quantidade) {
		return ResponseEntity.ok(pacienteService.listarPacientesPaginado(pagina, quantidade));
	}

}