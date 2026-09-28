package com.msp.odonto_pro.controller.relatorio;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.service.relatorio.RelatorioGeralService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.V1 + "/relatorios")
@RequiredArgsConstructor
public class RelatorioGeralController {

	private final RelatorioGeralService relatorioGeralService;

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Relatório geral consolidado (KPIs do mês atual) em PDF.")
	@GetMapping("/geral")
	public ResponseEntity<byte[]> geral() {
		final byte[] pdf = this.relatorioGeralService.gerarRelatorioPdf();
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-geral.pdf").body(pdf);
	}
}
