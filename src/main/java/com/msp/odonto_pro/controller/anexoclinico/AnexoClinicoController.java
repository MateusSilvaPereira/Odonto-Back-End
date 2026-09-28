package com.msp.odonto_pro.controller.anexoclinico;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;
import org.springframework.web.multipart.MultipartFile;

import com.msp.odonto_pro.dto.anexoclinico.AnexoClinicoDTO;
import com.msp.odonto_pro.entity.usuario.Usuario;
import com.msp.odonto_pro.service.anexoclinico.AnexoClinicoService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/**
 * Anexos clínicos do paciente (radiografia, foto intraoral, documento).
 * Sub-recurso de paciente, mesmo padrão de /pacientes/{id}/prontuario,
 * /pacientes/{id}/evolucoes etc. (ver PacienteClinicoController).
 */
@RestController
@RequestMapping(ApiPaths.V1 + "/pacientes/{pacienteId}/anexos")
@RequiredArgsConstructor
public class AnexoClinicoController {

	private final AnexoClinicoService anexoClinicoService;

	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Listar anexos clínicos do paciente.")
	@GetMapping
	public ResponseEntity<List<AnexoClinicoDTO>> listar(@PathVariable final Long pacienteId) {
		return ResponseEntity.ok(this.anexoClinicoService.listar(pacienteId));
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Enviar anexo clínico para o paciente.")
	@PostMapping
	public ResponseEntity<AnexoClinicoDTO> upload(@PathVariable final Long pacienteId,
			@RequestParam("arquivo") final MultipartFile arquivo, final Authentication authentication)
			throws IOException {
		final Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(this.anexoClinicoService.upload(pacienteId, arquivo, usuarioLogado));
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Excluir anexo clínico do paciente.")
	@DeleteMapping("/{anexoId}")
	public ResponseEntity<Void> excluir(@PathVariable final Long pacienteId, @PathVariable final Long anexoId) {
		this.anexoClinicoService.excluir(pacienteId, anexoId);
		return ResponseEntity.noContent().build();
	}
}
