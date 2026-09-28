package com.msp.odonto_pro.controller.orcamento;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.orcamento.OrcamentoDTO;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.service.orcamento.OrcamentoService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/**
 * Ações sobre um orçamento já existente. Mapeado em "/orcamentos"
 * (top-level) — listar/criar ficam em PacienteClinicoController, sob
 * "/pacientes/{id}/orcamentos".
 */
@RestController
@RequestMapping(ApiPaths.V1 + "/orcamentos")
@RequiredArgsConstructor
public class OrcamentoController {

	private final OrcamentoService orcamentoService;

	@PreAuthorize("hasAuthority('write:tratamento')")
	@Operation(summary = "Aprovar orçamento — gera um ItemTratamento por item.")
	@PatchMapping("/{id}/aprovar")
	public ResponseEntity<OrcamentoDTO> aprovar(@PathVariable final Long id, final Authentication authentication) {
		final Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
		return ResponseEntity.ok(this.orcamentoService.aprovar(id, usuarioLogado));
	}

	@PreAuthorize("hasAuthority('write:tratamento')")
	@Operation(summary = "Recusar orçamento.")
	@PatchMapping("/{id}/recusar")
	public ResponseEntity<OrcamentoDTO> recusar(@PathVariable final Long id) {
		return ResponseEntity.ok(this.orcamentoService.recusar(id));
	}

	@PreAuthorize("hasAuthority('delete:tratamento')")
	@Operation(summary = "Excluir orçamento (só permitido enquanto pendente).")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletar(@PathVariable final Long id) {
		this.orcamentoService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
