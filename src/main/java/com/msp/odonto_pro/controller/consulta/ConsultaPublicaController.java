package com.msp.odonto_pro.controller.consulta;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.consulta.ConsultaPublicaDTO;
import com.msp.odonto_pro.service.consulta.ConsultaPublicaService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints públicos (sem autenticação) usados pelo paciente para
 * confirmar/cancelar a própria consulta a partir do link enviado por
 * WhatsApp. Liberado em SecurityConfig sob o prefixo "/public/**".
 */
@RestController
@RequestMapping(ApiPaths.V1 + "/public/consultas")
@RequiredArgsConstructor
public class ConsultaPublicaController {

	private final ConsultaPublicaService consultaPublicaService;

	@Operation(summary = "Buscar dados públicos da consulta pelo token do link.")
	@GetMapping("/{token}")
	public ResponseEntity<ConsultaPublicaDTO> buscar(@PathVariable final String token) {
		return ResponseEntity.ok(this.consultaPublicaService.buscarPorToken(token));
	}

	@Operation(summary = "Paciente confirma a própria consulta pelo link.")
	@PostMapping("/{token}/confirmar")
	public ResponseEntity<ConsultaPublicaDTO> confirmar(@PathVariable final String token) {
		return ResponseEntity.ok(this.consultaPublicaService.confirmarPorToken(token));
	}

	@Operation(summary = "Paciente cancela a própria consulta pelo link.")
	@PostMapping("/{token}/cancelar")
	public ResponseEntity<ConsultaPublicaDTO> cancelar(@PathVariable final String token) {
		return ResponseEntity.ok(this.consultaPublicaService.cancelarPorToken(token));
	}
}
