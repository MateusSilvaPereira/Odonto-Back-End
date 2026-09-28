package com.msp.odonto_pro.controller.clinica;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.msp.odonto_pro.dto.clinica.ClinicaCreateRequest;
import com.msp.odonto_pro.dto.clinica.ClinicaDTO;
import com.msp.odonto_pro.dto.clinica.ClinicaUpdateRequest;
import com.msp.odonto_pro.service.clinica.ClinicaService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Gestão de clínicas (multi-tenant). CRUD por id: só SUPER_ADMIN. "Minha
 * clínica" (/me): ADMIN/SUPER_ADMIN, sempre derivada do token — substitui o
 * antigo singleton /configuracao-clinica.
 */
@RestController
@RequestMapping(ApiPaths.V1 + "/clinicas")
@RequiredArgsConstructor
public class ClinicaController {

	private final ClinicaService clinicaService;

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@Operation(summary = "Listar todas as clínicas")
	@GetMapping
	public ResponseEntity<List<ClinicaDTO>> listar() {
		return ResponseEntity.ok(this.clinicaService.listar());
	}

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@Operation(summary = "Criar uma clínica e seu usuário ADMIN inicial")
	@PostMapping
	public ResponseEntity<ClinicaDTO> criar(@Valid @RequestBody final ClinicaCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(this.clinicaService.criar(request));
	}

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@Operation(summary = "Atualizar dados cadastrais de uma clínica")
	@PutMapping("/{id}")
	public ResponseEntity<ClinicaDTO> atualizar(@PathVariable final Long id,
			@Valid @RequestBody final ClinicaUpdateRequest request) {
		return ResponseEntity.ok(this.clinicaService.atualizar(id, request.getNome(), request.getCnpj(),
				request.getTelefone(), request.getEndereco()));
	}

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@Operation(summary = "Ativar/desativar uma clínica")
	@PatchMapping("/{id}/toggle-active")
	public ResponseEntity<ClinicaDTO> toggleActive(@PathVariable final Long id) {
		return ResponseEntity.ok(this.clinicaService.toggleActive(id));
	}

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@Operation(summary = "Excluir uma clínica")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletar(@PathVariable final Long id) {
		this.clinicaService.deletar(id);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	@Operation(summary = "Obter os dados da própria clínica (derivada do token)")
	@GetMapping("/me")
	public ResponseEntity<ClinicaDTO> obterMinha() {
		return ResponseEntity.ok(this.clinicaService.obterMinha());
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	@Operation(summary = "Atualizar a própria clínica (sem trocar o logo)")
	@PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClinicaDTO> atualizarMinhaJson(@Valid @RequestBody final ClinicaUpdateRequest request)
			throws IOException {
		return ResponseEntity.ok(this.clinicaService.atualizarMinha(request.getNome(), request.getCnpj(),
				request.getTelefone(), request.getEndereco(), null));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	@Operation(summary = "Atualizar a própria clínica com logo novo")
	@PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ClinicaDTO> atualizarMinhaMultipart(@RequestParam("nome") final String nome,
			@RequestParam(value = "cnpj", required = false) final String cnpj,
			@RequestParam(value = "telefone", required = false) final String telefone,
			@RequestParam(value = "endereco", required = false) final String endereco,
			@RequestParam(value = "logo", required = false) final MultipartFile logo) throws IOException {
		return ResponseEntity.ok(this.clinicaService.atualizarMinha(nome, cnpj, telefone, endereco, logo));
	}
}
