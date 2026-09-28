package com.msp.odonto_pro.controller.listaespera;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.listaespera.ListaEsperaDTO;
import com.msp.odonto_pro.service.listaespera.ListaEsperaService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/** Fila de espera para encaixe de consulta. */
@RestController
@RequestMapping(ApiPaths.V1 + "/lista-espera")
@RequiredArgsConstructor
public class ListaEsperaController {

	private final ListaEsperaService listaEsperaService;

	@PreAuthorize("hasAuthority('read:consulta')")
	@Operation(summary = "Listar itens da lista de espera.")
	@GetMapping
	public ResponseEntity<List<ListaEsperaDTO>> listar() {
		return ResponseEntity.ok(this.listaEsperaService.listar());
	}

	@PreAuthorize("hasAuthority('write:consulta')")
	@Operation(summary = "Adicionar paciente à lista de espera.")
	@PostMapping
	public ResponseEntity<ListaEsperaDTO> criar(@RequestBody final ListaEsperaDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(this.listaEsperaService.criar(dto));
	}

	@PreAuthorize("hasAuthority('write:consulta')")
	@Operation(summary = "Marcar item da lista de espera como agendado.")
	@PatchMapping("/{id}/agendado")
	public ResponseEntity<ListaEsperaDTO> marcarAgendado(@PathVariable final Long id) {
		return ResponseEntity.ok(this.listaEsperaService.marcarAgendado(id));
	}

	@PreAuthorize("hasAuthority('write:consulta')")
	@Operation(summary = "Remover item da lista de espera.")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable final Long id) {
		this.listaEsperaService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
