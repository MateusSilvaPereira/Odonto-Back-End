package com.msp.odonto_pro.controller.orcamento;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.orcamento.OrcamentoPublicoDTO;
import com.msp.odonto_pro.service.orcamento.OrcamentoPublicoService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints públicos (sem autenticação) usados pelo paciente para
 * aprovar/recusar o próprio orçamento a partir do link compartilhado pela
 * clínica. Liberado em SecurityConfig sob o prefixo "/public/**".
 */
@RestController
@RequestMapping(ApiPaths.V1 + "/public/orcamentos")
@RequiredArgsConstructor
public class OrcamentoPublicoController {

	private final OrcamentoPublicoService orcamentoPublicoService;

	@Operation(summary = "Buscar dados públicos do orçamento pelo token do link.")
	@GetMapping("/{token}")
	public ResponseEntity<OrcamentoPublicoDTO> buscar(@PathVariable final String token) {
		return ResponseEntity.ok(this.orcamentoPublicoService.buscarPorToken(token));
	}

	@Operation(summary = "Paciente aprova o próprio orçamento pelo link.")
	@PostMapping("/{token}/aprovar")
	public ResponseEntity<OrcamentoPublicoDTO> aprovar(@PathVariable final String token) {
		return ResponseEntity.ok(this.orcamentoPublicoService.aprovarPorToken(token));
	}

	@Operation(summary = "Paciente recusa o próprio orçamento pelo link.")
	@PostMapping("/{token}/recusar")
	public ResponseEntity<OrcamentoPublicoDTO> recusar(@PathVariable final String token) {
		return ResponseEntity.ok(this.orcamentoPublicoService.recusarPorToken(token));
	}
}
