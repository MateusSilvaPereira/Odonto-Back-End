package com.msp.odonto_pro.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msp.odonto_pro.dtos.PagamentoDTO;
import com.msp.odonto_pro.services.FinanceiroService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

	private final FinanceiroService financeiroService;

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Registrar pagamento contra um item de tratamento.")
	@PostMapping
	public ResponseEntity<PagamentoDTO> criar(@RequestBody final PagamentoDTO dto) {
		return ResponseEntity.ok(this.financeiroService.registrarPagamento(dto));
	}
}
