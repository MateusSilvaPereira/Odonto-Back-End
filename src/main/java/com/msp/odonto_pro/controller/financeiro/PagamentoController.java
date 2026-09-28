package com.msp.odonto_pro.controller.financeiro;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.financeiro.PagamentoDTO;
import com.msp.odonto_pro.service.financeiro.FinanceiroService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.V1 + "/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

	private final FinanceiroService financeiroService;

	@PreAuthorize("hasAuthority('write:financeiro')")
	@Operation(summary = "Registrar pagamento contra um item de tratamento.")
	@PostMapping
	public ResponseEntity<PagamentoDTO> criar(@RequestBody final PagamentoDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(this.financeiroService.registrarPagamento(dto));
	}
}
