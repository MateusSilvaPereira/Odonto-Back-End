package com.msp.odonto_pro.controller.recall;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.recall.RecallConfigDTO;
import com.msp.odonto_pro.dto.recall.RecallEnviarRequest;
import com.msp.odonto_pro.dto.recall.RecallEnvioResultadoDTO;
import com.msp.odonto_pro.service.recall.RecallConfigService;
import com.msp.odonto_pro.service.recall.RecallService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Campanha de recall (reativação de pacientes) por WhatsApp/e-mail. */
@RestController
@RequestMapping(ApiPaths.V1 + "/recall")
@RequiredArgsConstructor
public class RecallController {

	private final RecallConfigService recallConfigService;
	private final RecallService recallService;

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Obter configuração da campanha de recall.")
	@GetMapping("/config")
	public ResponseEntity<RecallConfigDTO> obterConfig() {
		return ResponseEntity.ok(this.recallConfigService.obterDto());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Atualizar configuração da campanha de recall.")
	@PutMapping("/config")
	public ResponseEntity<RecallConfigDTO> atualizarConfig(@RequestBody final RecallConfigDTO dto) {
		return ResponseEntity.ok(this.recallConfigService.atualizar(dto));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Disparar recall agora para os pacientes indicados.")
	@PostMapping("/enviar")
	public ResponseEntity<List<RecallEnvioResultadoDTO>> enviar(@Valid @RequestBody final RecallEnviarRequest request) {
		return ResponseEntity.ok(this.recallService.enviar(request.getPacienteIds()));
	}
}
