package com.msp.odonto_pro.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msp.odonto_pro.dtos.PacienteDTO;
import com.msp.odonto_pro.dtos.PageResponseDTO;
import com.msp.odonto_pro.projections.pacientes.PacienteProjection;
import com.msp.odonto_pro.services.PacienteService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/**
 * Controller de pacientes.
 * 
 * Cada método tem @PreAuthorize para controle granular de acesso. A verificação
 * é feita em duas camadas: 1. SecurityConfig: verifica se a URL é acessível
 * pelo Role 2. @PreAuthorize: verifica se tem a permissão específica
 */
@RestController
@RequestMapping(value = "/paciente")
@RequiredArgsConstructor
public class PacienteController {

	private final PacienteService pacienteService;

	/**
	 * Salva um novo paciente. Requer permissão "write:paciente".
	 */
	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Salvar paciente.")
	@PostMapping("/salvar")
	public ResponseEntity<PacienteDTO> salvar(@RequestBody final PacienteDTO pacienteDTO) {
		return ResponseEntity.ok(this.pacienteService.save(pacienteDTO));
	}

	/**
	 * Deleta um paciente por ID. Requer permissão "delete:paciente" (apenas ADMIN).
	 */
	@PreAuthorize("hasAuthority('delete:paciente')")
	@Operation(description = "Deletar Paciente por id.")
	@DeleteMapping(value = "/delete/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable final Long id) {
		this.pacienteService.excluir(id);
		return ResponseEntity.ok().build();
	}

	/**
	 * Lista pacientes paginado. Requer permissão "read:paciente".
	 */
	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Listar pacientes paginado")
	@GetMapping("/listar")
	public ResponseEntity<PageResponseDTO<PacienteProjection>> listar(
			@RequestParam(value = "p", required = false, defaultValue = "1") final Integer pagina,
			@RequestParam(value = "q", required = false, defaultValue = "50") final Integer quantidade) {
		final Page<PacienteProjection> page = this.pacienteService.listarPacientesPaginado(pagina, quantidade);
		return ResponseEntity.ok(new PageResponseDTO<>(page.getContent(), page.getTotalElements(),
				page.getTotalPages(), page.getNumber()));
	}

	/**
	 * Lista todos os pacientes, sem paginação. Usado na tela de Relatórios.
	 */
	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Listar todos os pacientes, sem paginação.")
	@GetMapping("/buscar-todos")
	public ResponseEntity<List<PacienteDTO>> buscarTodos() {
		return ResponseEntity.ok(this.pacienteService.listarTodos());
	}

	/**
	 * Gera um relatório em PDF com a listagem completa de pacientes.
	 */
	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Gerar relatório de pacientes em PDF.")
	@GetMapping("/relatorio")
	public ResponseEntity<byte[]> relatorio() {
		final byte[] pdf = this.pacienteService.gerarRelatorioPdf();
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-pacientes.pdf").body(pdf);
	}
}