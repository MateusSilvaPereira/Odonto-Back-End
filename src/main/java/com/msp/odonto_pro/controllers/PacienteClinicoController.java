package com.msp.odonto_pro.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msp.odonto_pro.dtos.DenteRegistroDTO;
import com.msp.odonto_pro.dtos.ItemTratamentoDTO;
import com.msp.odonto_pro.services.ItemTratamentoService;
import com.msp.odonto_pro.services.OdontogramaService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Sub-recursos clínicos de um paciente: odontograma e plano de tratamento.
 * Mapeado em "/pacientes" (plural), distinto do CRUD legado em "/paciente".
 */
@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteClinicoController {

	private final OdontogramaService odontogramaService;
	private final ItemTratamentoService itemTratamentoService;

	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Obter odontograma do paciente.")
	@GetMapping("/{id}/odontograma")
	public ResponseEntity<OdontogramaResponse> obterOdontograma(@PathVariable final Long id) {
		return ResponseEntity.ok(new OdontogramaResponse(this.odontogramaService.listar(id)));
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Atualizar status/observação de um dente do odontograma.")
	@PutMapping("/{id}/odontograma/dente/{numero}")
	public ResponseEntity<DenteRegistroDTO> atualizarDente(@PathVariable final Long id,
			@PathVariable final String numero, @RequestBody final DenteRegistroDTO dto) {
		return ResponseEntity.ok(this.odontogramaService.atualizarDente(id, numero, dto));
	}

	@PreAuthorize("hasAuthority('read:tratamento')")
	@Operation(summary = "Listar plano de tratamento do paciente.")
	@GetMapping("/{id}/tratamentos")
	public ResponseEntity<List<ItemTratamentoDTO>> listarTratamentos(@PathVariable final Long id) {
		return ResponseEntity.ok(this.itemTratamentoService.listarPorPaciente(id));
	}

	@PreAuthorize("hasAuthority('write:tratamento')")
	@Operation(summary = "Adicionar item ao plano de tratamento do paciente.")
	@PostMapping("/{id}/tratamentos")
	public ResponseEntity<ItemTratamentoDTO> criarTratamento(@PathVariable final Long id,
			@RequestBody final ItemTratamentoDTO dto) {
		return ResponseEntity.ok(this.itemTratamentoService.criar(id, dto));
	}

	@Data
	@AllArgsConstructor
	public static class OdontogramaResponse {
		private List<DenteRegistroDTO> dentes;
	}
}
