package com.msp.odonto_pro.controllers;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msp.odonto_pro.dtos.FinanceiroResumoDTO;
import com.msp.odonto_pro.dtos.PagamentoDTO;
import com.msp.odonto_pro.dtos.PendenciaTratamentoDTO;
import com.msp.odonto_pro.services.FinanceiroService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/financeiro")
@RequiredArgsConstructor
public class FinanceiroController {

	private final FinanceiroService financeiroService;

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Resumo financeiro do mês/ano informado.")
	@GetMapping("/resumo")
	public ResponseEntity<FinanceiroResumoDTO> resumo(@RequestParam final int mes, @RequestParam final int ano) {
		return ResponseEntity.ok(this.financeiroService.resumo(mes, ano));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Pagamentos registrados no mês/ano informado.")
	@GetMapping("/pagamentos")
	public ResponseEntity<List<PagamentoDTO>> pagamentos(@RequestParam final int mes, @RequestParam final int ano) {
		return ResponseEntity.ok(this.financeiroService.listarPagamentos(mes, ano));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Itens de tratamento com saldo pendente, de todos os pacientes.")
	@GetMapping("/pendencias")
	public ResponseEntity<List<PendenciaTratamentoDTO>> pendencias() {
		return ResponseEntity.ok(this.financeiroService.listarPendencias());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Gerar relatório financeiro do mês/ano em PDF.")
	@GetMapping("/relatorio")
	public ResponseEntity<byte[]> relatorio(@RequestParam final int mes, @RequestParam final int ano) {
		final byte[] pdf = this.financeiroService.gerarRelatorioPdf(mes, ano);
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-financeiro.pdf").body(pdf);
	}
}
