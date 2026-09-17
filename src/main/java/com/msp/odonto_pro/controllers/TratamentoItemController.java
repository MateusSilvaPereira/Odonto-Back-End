package com.msp.odonto_pro.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msp.odonto_pro.dtos.ItemTratamentoDTO;
import com.msp.odonto_pro.services.ItemTratamentoService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/**
 * Edição/exclusão de um item do plano de tratamento já existente. Mapeado em
 * "/tratamentos" (plural) — não confundir com "/tratamento" (singular),
 * controller legado ligado à entidade Tratamento antiga.
 */
@RestController
@RequestMapping("/tratamentos")
@RequiredArgsConstructor
public class TratamentoItemController {

	private final ItemTratamentoService itemTratamentoService;

	@PreAuthorize("hasAuthority('write:tratamento')")
	@Operation(summary = "Atualizar item do plano de tratamento.")
	@PutMapping("/{id}")
	public ResponseEntity<ItemTratamentoDTO> atualizar(@PathVariable final Long id,
			@RequestBody final ItemTratamentoDTO dto) {
		return ResponseEntity.ok(this.itemTratamentoService.atualizar(id, dto));
	}

	@PreAuthorize("hasAuthority('delete:tratamento')")
	@Operation(summary = "Excluir item do plano de tratamento.")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletar(@PathVariable final Long id) {
		this.itemTratamentoService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
