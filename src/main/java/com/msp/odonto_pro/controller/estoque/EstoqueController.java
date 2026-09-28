package com.msp.odonto_pro.controller.estoque;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.estoque.ItemEstoqueDTO;
import com.msp.odonto_pro.dto.estoque.MovimentacaoEstoqueDTO;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.service.estoque.EstoqueService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.V1 + "/estoque")
@RequiredArgsConstructor
public class EstoqueController {

	private final EstoqueService estoqueService;

	@PreAuthorize("hasAuthority('read:estoque')")
	@Operation(summary = "Listar itens de estoque.")
	@GetMapping("/itens")
	public ResponseEntity<List<ItemEstoqueDTO>> listarItens() {
		return ResponseEntity.ok(this.estoqueService.listarItens());
	}

	@PreAuthorize("hasAuthority('write:estoque')")
	@Operation(summary = "Cadastrar item de estoque.")
	@PostMapping("/itens")
	public ResponseEntity<ItemEstoqueDTO> criarItem(@RequestBody final ItemEstoqueDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(this.estoqueService.criarItem(dto));
	}

	@PreAuthorize("hasAuthority('write:estoque')")
	@Operation(summary = "Atualizar item de estoque.")
	@PutMapping("/itens/{id}")
	public ResponseEntity<ItemEstoqueDTO> atualizarItem(@PathVariable final Long id,
			@RequestBody final ItemEstoqueDTO dto) {
		return ResponseEntity.ok(this.estoqueService.atualizarItem(id, dto));
	}

	@PreAuthorize("hasAuthority('delete:estoque')")
	@Operation(summary = "Excluir item de estoque por id.")
	@DeleteMapping("/itens/{id}")
	public ResponseEntity<Void> excluirItem(@PathVariable final Long id) {
		this.estoqueService.excluirItem(id);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAuthority('read:estoque')")
	@Operation(summary = "Listar movimentações de estoque.")
	@GetMapping("/movimentacoes")
	public ResponseEntity<List<MovimentacaoEstoqueDTO>> listarMovimentacoes() {
		return ResponseEntity.ok(this.estoqueService.listarMovimentacoes());
	}

	@PreAuthorize("hasAuthority('write:estoque')")
	@Operation(summary = "Registrar entrada ou saída de um item de estoque.")
	@PostMapping("/movimentacoes")
	public ResponseEntity<MovimentacaoEstoqueDTO> registrarMovimentacao(@RequestBody final MovimentacaoEstoqueDTO dto,
			final Authentication authentication) {
		final Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(this.estoqueService.registrarMovimentacao(dto, usuarioLogado));
	}
}
