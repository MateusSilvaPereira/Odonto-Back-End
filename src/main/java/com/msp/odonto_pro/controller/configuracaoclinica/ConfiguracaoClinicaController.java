package com.msp.odonto_pro.controller.configuracaoclinica;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;
import org.springframework.web.multipart.MultipartFile;

import com.msp.odonto_pro.dto.configuracaoclinica.ConfiguracaoClinicaDTO;
import com.msp.odonto_pro.service.configuracaoclinica.ConfiguracaoClinicaService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/** Dados da clínica (singleton). Só ADMIN acessa, mesmo padrão de /usuario. */
@RestController
@RequestMapping(ApiPaths.V1 + "/configuracao-clinica")
@RequiredArgsConstructor
public class ConfiguracaoClinicaController {

	private final ConfiguracaoClinicaService configuracaoClinicaService;

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Obter a configuração atual da clínica.")
	@GetMapping
	public ResponseEntity<ConfiguracaoClinicaDTO> obter() {
		return ResponseEntity.ok(this.configuracaoClinicaService.obter());
	}

	/** Edição via JSON puro (sem logo novo). */
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Atualizar dados da clínica (sem trocar o logo).")
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ConfiguracaoClinicaDTO> atualizarJson(@RequestBody final ConfiguracaoClinicaDTO dto)
			throws IOException {
		return ResponseEntity.ok(this.configuracaoClinicaService.atualizar(dto.getNome(), dto.getCnpj(),
				dto.getTelefone(), dto.getEndereco(), null));
	}

	/** Edição via multipart/form-data, usada quando o front envia um logo novo. */
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Atualizar dados da clínica com logo novo.")
	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ConfiguracaoClinicaDTO> atualizarMultipart(
			@RequestParam("nome") final String nome,
			@RequestParam(value = "cnpj", required = false) final String cnpj,
			@RequestParam(value = "telefone", required = false) final String telefone,
			@RequestParam(value = "endereco", required = false) final String endereco,
			@RequestParam(value = "logo", required = false) final MultipartFile logo) throws IOException {
		return ResponseEntity.ok(this.configuracaoClinicaService.atualizar(nome, cnpj, telefone, endereco, logo));
	}
}
