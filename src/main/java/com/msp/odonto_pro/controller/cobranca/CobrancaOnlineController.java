package com.msp.odonto_pro.controller.cobranca;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.cobranca.CobrancaCreateRequest;
import com.msp.odonto_pro.dto.cobranca.CobrancaOnlineDTO;
import com.msp.odonto_pro.service.cobranca.CobrancaOnlineService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Cobrança online (Pix/Boleto) contra um item de tratamento. MOCK
 * temporário: sem integração real com o Mercado Pago ainda (ver
 * CobrancaOnlineService). O endpoint {@code simular-pagamento} é só de
 * teste e deve ser removido/desabilitado quando a integração real entrar.
 */
@RestController
@RequestMapping(ApiPaths.V1 + "/cobrancas")
@RequiredArgsConstructor
public class CobrancaOnlineController {

	private final CobrancaOnlineService cobrancaOnlineService;

	@PreAuthorize("hasAuthority('write:financeiro')")
	@Operation(summary = "Criar uma cobrança Pix/Boleto (mock).")
	@PostMapping
	public ResponseEntity<CobrancaOnlineDTO> criar(@Valid @RequestBody final CobrancaCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(this.cobrancaOnlineService.criar(request));
	}

	@PreAuthorize("hasAuthority('read:financeiro')")
	@Operation(summary = "Consultar o status atual de uma cobrança.")
	@GetMapping("/{id}")
	public ResponseEntity<CobrancaOnlineDTO> obter(@PathVariable final Long id) {
		return ResponseEntity.ok(this.cobrancaOnlineService.obter(id));
	}

	/**
	 * Endpoint só de teste: simula a confirmação de pagamento (webhook do
	 * Mercado Pago, na integração real) e gera o Pagamento correspondente.
	 * Remover/desabilitar quando a integração real entrar.
	 */
	@PreAuthorize("hasAuthority('write:financeiro')")
	@Operation(summary = "[MOCK] Simular a confirmação de pagamento desta cobrança.")
	@PostMapping("/{id}/simular-pagamento")
	public ResponseEntity<CobrancaOnlineDTO> simularPagamento(@PathVariable final Long id) {
		return ResponseEntity.ok(this.cobrancaOnlineService.simularPagamento(id));
	}
}
