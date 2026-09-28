package com.msp.odonto_pro.controller.dentista;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;
import org.springframework.web.multipart.MultipartFile;

import com.msp.odonto_pro.dto.dentista.DentistaDTO;
import com.msp.odonto_pro.service.dentista.DentistaService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/**
 * Cadastro de perfil profissional do dentista. Não deve ser confundido com
 * o login (ver /usuario e /auth): aqui não há usuário/senha, apenas os
 * dados usados na agenda e no prontuário (CRO, especialidade, cor etc.).
 */
@RestController
@RequestMapping(ApiPaths.V1 + "/dentistas")
@RequiredArgsConstructor
public class DentistaController {

	private final DentistaService dentistaService;

	@PreAuthorize("hasAuthority('read:dentista')")
	@Operation(summary = "Listar dentistas.")
	@GetMapping
	public ResponseEntity<List<DentistaDTO>> listar() {
		return ResponseEntity.ok(this.dentistaService.listar());
	}

	@PreAuthorize("hasAuthority('write:dentista')")
	@Operation(summary = "Cadastrar dentista.")
	@PostMapping
	public ResponseEntity<DentistaDTO> criar(@RequestBody final DentistaDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(this.dentistaService.criar(dto));
	}

	@PreAuthorize("hasAuthority('write:dentista')")
	@Operation(summary = "Atualizar dentista.")
	@PutMapping("/{id}")
	public ResponseEntity<DentistaDTO> atualizar(@PathVariable final Long id, @RequestBody final DentistaDTO dto) {
		return ResponseEntity.ok(this.dentistaService.atualizar(id, dto));
	}

	@PreAuthorize("hasAuthority('write:dentista')")
	@Operation(summary = "Ativar/desativar dentista.")
	@PatchMapping("/{id}/toggle-active")
	public ResponseEntity<DentistaDTO> toggleActive(@PathVariable final Long id) {
		return ResponseEntity.ok(this.dentistaService.toggleActive(id));
	}

	@PreAuthorize("hasAuthority('write:dentista')")
	@Operation(summary = "Enviar foto do dentista.")
	@PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<DentistaDTO> uploadFoto(@PathVariable final Long id,
			@RequestParam("foto") final MultipartFile foto) throws IOException {
		return ResponseEntity.ok(this.dentistaService.atualizarFoto(id, foto));
	}

	@PreAuthorize("hasAuthority('write:dentista')")
	@Operation(summary = "Remover foto do dentista.")
	@DeleteMapping("/{id}/foto")
	public ResponseEntity<DentistaDTO> removerFoto(@PathVariable final Long id) {
		return ResponseEntity.ok(this.dentistaService.removerFoto(id));
	}

	@PreAuthorize("hasAuthority('delete:dentista')")
	@Operation(summary = "Excluir dentista por id.")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable final Long id) {
		this.dentistaService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
