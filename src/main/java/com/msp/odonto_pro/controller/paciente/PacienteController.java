package com.msp.odonto_pro.controller.paciente;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.msp.odonto_pro.dto.paciente.PacienteDTO;
import com.msp.odonto_pro.dto.shared.PageResponseDTO;
import com.msp.odonto_pro.dto.paciente.PacienteProjection;
import com.msp.odonto_pro.service.paciente.PacienteService;
import com.msp.odonto_pro.web.ApiPaths;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.V1 + "/pacientes")
@RequiredArgsConstructor
public class PacienteController {

	private final PacienteService pacienteService;

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Criar paciente.")
	@PostMapping
	public ResponseEntity<PacienteDTO> salvar(@Valid @RequestBody final PacienteDTO pacienteDTO) {
		return ResponseEntity.status(HttpStatus.CREATED).body(this.pacienteService.save(pacienteDTO));
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Atualizar paciente.")
	@PutMapping("/{id}")
	public ResponseEntity<PacienteDTO> atualizar(@PathVariable final Long id,
			@Valid @RequestBody final PacienteDTO pacienteDTO) {
		return ResponseEntity.ok(this.pacienteService.atualizar(id, pacienteDTO));
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Enviar foto do paciente.")
	@PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<PacienteDTO> uploadFoto(@PathVariable final Long id,
			@RequestParam("foto") final MultipartFile foto) throws IOException {
		return ResponseEntity.ok(this.pacienteService.atualizarFoto(id, foto));
	}

	@PreAuthorize("hasAuthority('write:paciente')")
	@Operation(summary = "Remover foto do paciente.")
	@DeleteMapping("/{id}/foto")
	public ResponseEntity<PacienteDTO> removerFoto(@PathVariable final Long id) {
		return ResponseEntity.ok(this.pacienteService.removerFoto(id));
	}

	@PreAuthorize("hasAuthority('delete:paciente')")
	@Operation(description = "Deletar Paciente por id.")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable final Long id) {
		this.pacienteService.excluir(id);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Listar pacientes paginado")
	@GetMapping
	public ResponseEntity<PageResponseDTO<PacienteProjection>> listar(
			@RequestParam(value = "p", required = false, defaultValue = "1") final Integer pagina,
			@RequestParam(value = "q", required = false, defaultValue = "50") final Integer quantidade) {
		final Page<PacienteProjection> page = this.pacienteService.listarPacientesPaginado(pagina, quantidade);
		return ResponseEntity.ok(new PageResponseDTO<>(page.getContent(), page.getTotalElements(),
				page.getTotalPages(), page.getNumber()));
	}

	// Sem paginação — usado pela tela de Relatórios, que precisa da base completa de pacientes.
	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Listar todos os pacientes, sem paginação.")
	@GetMapping("/todos")
	public ResponseEntity<List<PacienteDTO>> buscarTodos() {
		return ResponseEntity.ok(this.pacienteService.listarTodos());
	}

	@PreAuthorize("hasAuthority('read:paciente')")
	@Operation(summary = "Gerar relatório de pacientes em PDF.")
	@GetMapping("/relatorio")
	public ResponseEntity<byte[]> relatorio() {
		final byte[] pdf = this.pacienteService.gerarRelatorioPdf();
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-pacientes.pdf").body(pdf);
	}
}