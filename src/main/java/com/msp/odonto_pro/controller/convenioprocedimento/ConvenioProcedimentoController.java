package com.msp.odonto_pro.controller.convenioprocedimento;

import java.util.List;

import org.springframework.http.HttpStatus;
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
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.convenioprocedimento.ConvenioProcedimentoDTO;
import com.msp.odonto_pro.service.convenioprocedimento.ConvenioProcedimentoService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/** Tabela de procedimentos/valores de um convênio (sub-recurso de Convenio). */
@RestController
@RequestMapping(ApiPaths.V1 + "/convenios/{convenioId}/procedimentos")
@RequiredArgsConstructor
public class ConvenioProcedimentoController {

	private final ConvenioProcedimentoService convenioProcedimentoService;

	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Listar os procedimentos/valores de um convênio.")
	@GetMapping
	public ResponseEntity<List<ConvenioProcedimentoDTO>> listar(@PathVariable final Long convenioId) {
		return ResponseEntity.ok(this.convenioProcedimentoService.listar(convenioId));
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Cadastrar um procedimento/valor para o convênio.")
	@PostMapping
	public ResponseEntity<ConvenioProcedimentoDTO> criar(@PathVariable final Long convenioId,
			@RequestBody final ConvenioProcedimentoDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(this.convenioProcedimentoService.criar(convenioId, dto));
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Atualizar um procedimento/valor do convênio.")
	@PutMapping("/{id}")
	public ResponseEntity<ConvenioProcedimentoDTO> atualizar(@PathVariable final Long convenioId,
			@PathVariable final Long id, @RequestBody final ConvenioProcedimentoDTO dto) {
		return ResponseEntity.ok(this.convenioProcedimentoService.atualizar(convenioId, id, dto));
	}

	@PreAuthorize("hasAuthority('delete:paciente')")
	@Operation(summary = "Excluir um procedimento/valor do convênio.")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable final Long convenioId, @PathVariable final Long id) {
		this.convenioProcedimentoService.excluir(convenioId, id);
		return ResponseEntity.noContent().build();
	}
}
