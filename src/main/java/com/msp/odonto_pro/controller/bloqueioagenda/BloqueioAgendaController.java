package com.msp.odonto_pro.controller.bloqueioagenda;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.bloqueioagenda.BloqueioAgendaDTO;
import com.msp.odonto_pro.service.bloqueioagenda.BloqueioAgendaService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/** Bloqueios de agenda do dentista (férias, folgas etc.), sub-recurso de Dentista. */
@RestController
@RequestMapping(ApiPaths.V1 + "/bloqueios-agenda")
@RequiredArgsConstructor
public class BloqueioAgendaController {

	private final BloqueioAgendaService bloqueioAgendaService;

	@PreAuthorize("hasAuthority('read:dentista')")
	@Operation(summary = "Listar bloqueios de agenda.")
	@GetMapping
	public ResponseEntity<List<BloqueioAgendaDTO>> listar() {
		return ResponseEntity.ok(this.bloqueioAgendaService.listar());
	}

	@PreAuthorize("hasAuthority('write:dentista')")
	@Operation(summary = "Criar bloqueio de agenda.")
	@PostMapping
	public ResponseEntity<BloqueioAgendaDTO> criar(@RequestBody final BloqueioAgendaDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(this.bloqueioAgendaService.criar(dto));
	}

	@PreAuthorize("hasAuthority('write:dentista')")
	@Operation(summary = "Excluir bloqueio de agenda por id.")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable final Long id) {
		this.bloqueioAgendaService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
