package com.msp.odonto_pro.controller.auditoria;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.auditoria.AuditoriaAcessoDTO;
import com.msp.odonto_pro.dto.shared.PageResponseDTO;
import com.msp.odonto_pro.service.auditoria.AuditoriaAcessoService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/** Auditoria (LGPD) de acesso a ações sensíveis sobre dados de paciente. */
@RestController
@RequestMapping(ApiPaths.V1 + "/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

	private final AuditoriaAcessoService auditoriaAcessoService;

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Listar registros de auditoria (LGPD), paginado.")
	@GetMapping
	public ResponseEntity<PageResponseDTO<AuditoriaAcessoDTO>> listar(
			@RequestParam(required = false) final Integer pagina, @RequestParam(required = false) final Integer tamanho,
			@RequestParam(required = false) final String busca,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate dataInicio,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate dataFim) {
		return ResponseEntity.ok(this.auditoriaAcessoService.listar(pagina, tamanho, busca, dataInicio, dataFim));
	}
}
