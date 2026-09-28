package com.msp.odonto_pro.controller.convenio;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.convenio.ConvenioDTO;
import com.msp.odonto_pro.service.convenio.ConvenioService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/** Cadastro de convênios/planos de saúde, vinculável a Paciente. */
@RestController
@RequestMapping(ApiPaths.V1 + "/convenios")
@RequiredArgsConstructor
public class ConvenioController {

	private final ConvenioService convenioService;

	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Listar convênios.")
	@GetMapping
	public ResponseEntity<List<ConvenioDTO>> listar() {
		return ResponseEntity.ok(this.convenioService.listar());
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Cadastrar convênio.")
	@PostMapping
	public ResponseEntity<ConvenioDTO> criar(@RequestBody final ConvenioDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(this.convenioService.criar(dto));
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Atualizar convênio.")
	@PutMapping("/{id}")
	public ResponseEntity<ConvenioDTO> atualizar(@PathVariable final Long id, @RequestBody final ConvenioDTO dto) {
		return ResponseEntity.ok(this.convenioService.atualizar(id, dto));
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Ativar/desativar convênio.")
	@PatchMapping("/{id}/toggle-active")
	public ResponseEntity<ConvenioDTO> toggleActive(@PathVariable final Long id) {
		return ResponseEntity.ok(this.convenioService.toggleActive(id));
	}

	@PreAuthorize("hasAuthority('delete:paciente')")
	@Operation(summary = "Excluir convênio por id.")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable final Long id) {
		this.convenioService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
