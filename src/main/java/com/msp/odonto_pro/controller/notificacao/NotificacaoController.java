package com.msp.odonto_pro.controller.notificacao;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.notificacao.NotificacaoConfigDTO;
import com.msp.odonto_pro.service.notificacao.NotificacaoConfigService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.V1 + "/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

	private final NotificacaoConfigService notificacaoConfigService;

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Obter configuração dos lembretes automáticos por WhatsApp.")
	@GetMapping("/config")
	public ResponseEntity<NotificacaoConfigDTO> obterConfig() {
		return ResponseEntity.ok(this.notificacaoConfigService.obterDto());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Atualizar configuração dos lembretes automáticos por WhatsApp.")
	@PutMapping("/config")
	public ResponseEntity<NotificacaoConfigDTO> atualizarConfig(@RequestBody final NotificacaoConfigDTO dto) {
		return ResponseEntity.ok(this.notificacaoConfigService.atualizar(dto));
	}
}
