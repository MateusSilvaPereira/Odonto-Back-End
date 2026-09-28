package com.msp.odonto_pro.controller.termoconsentimento;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.termoconsentimento.TermoConsentimentoDTO;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.service.termoconsentimento.TermoConsentimentoService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/**
 * Registro de auditoria de termos de consentimento (LGPD) assinados
 * fisicamente pelo paciente. Sub-recurso de paciente, append-only (sem
 * PUT/DELETE — ver TermoConsentimentoService).
 */
@RestController
@RequestMapping(ApiPaths.V1 + "/pacientes/{pacienteId}/termos-consentimento")
@RequiredArgsConstructor
public class TermoConsentimentoController {

	private final TermoConsentimentoService termoConsentimentoService;

	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Listar termos de consentimento registrados do paciente.")
	@GetMapping
	public ResponseEntity<List<TermoConsentimentoDTO>> listar(@PathVariable final Long pacienteId) {
		return ResponseEntity.ok(this.termoConsentimentoService.listar(pacienteId));
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Registrar termo de consentimento como assinado.")
	@PostMapping
	public ResponseEntity<TermoConsentimentoDTO> registrar(@PathVariable final Long pacienteId,
			@RequestBody final TermoConsentimentoDTO dto, final Authentication authentication) throws IOException {
		final Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(this.termoConsentimentoService.registrar(pacienteId, dto, usuarioLogado));
	}
}
