package com.msp.odonto_pro.controllers;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msp.odonto_pro.dtos.ConsultaDTO;
import com.msp.odonto_pro.services.ConsultaService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/consultas")
@RequiredArgsConstructor
public class ConsultaController {

	private final ConsultaService consultaService;

	@PreAuthorize("hasAuthority('read:consulta')")
	@Operation(summary = "Listar consultas.")
	@GetMapping
	public ResponseEntity<List<ConsultaDTO>> listar() {
		return ResponseEntity.ok(this.consultaService.listar());
	}

	@PreAuthorize("hasAuthority('write:consulta')")
	@Operation(summary = "Criar consulta.")
	@PostMapping
	public ResponseEntity<ConsultaDTO> criar(@RequestBody final ConsultaDTO dto) {
		return ResponseEntity.ok(this.consultaService.criar(dto));
	}

	@PreAuthorize("hasAuthority('write:consulta')")
	@Operation(summary = "Atualizar status da consulta.")
	@PutMapping("/{id}/status")
	public ResponseEntity<ConsultaDTO> atualizarStatus(@PathVariable final Long id,
			@RequestBody final StatusRequest request) {
		return ResponseEntity.ok(this.consultaService.atualizarStatus(id, request.getStatus()));
	}

	@PreAuthorize("hasAuthority('delete:consulta')")
	@Operation(description = "Excluir consulta por id.")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletar(@PathVariable final Long id) {
		this.consultaService.excluir(id);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAuthority('read:consulta')")
	@Operation(summary = "Gerar relatório de consultas em PDF.")
	@GetMapping("/relatorio")
	public ResponseEntity<byte[]> relatorio() {
		final byte[] pdf = this.consultaService.gerarRelatorioPdf();
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-consultas.pdf").body(pdf);
	}

	@Data
	public static class StatusRequest {
		private String status;
	}
}
