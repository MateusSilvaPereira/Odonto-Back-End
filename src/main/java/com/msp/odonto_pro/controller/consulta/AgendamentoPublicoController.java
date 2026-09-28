package com.msp.odonto_pro.controller.consulta;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.consulta.PublicAgendamentoRequest;
import com.msp.odonto_pro.dto.consulta.PublicAgendamentoResponseDTO;
import com.msp.odonto_pro.dto.consulta.PublicDentistaDTO;
import com.msp.odonto_pro.service.consulta.AgendamentoPublicoService;
import com.msp.odonto_pro.service.consulta.PublicBookingRateLimiter;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Agendamento online público (sem autenticação), sob "/public/**" (liberado
 * em SecurityConfig). O id da clínica vem sempre da URL — é assim que o
 * widget de agendamento sabe em qual clínica está marcando (cada clínica
 * recebe um link próprio com seu id).
 */
@RestController
@RequestMapping(ApiPaths.V1 + "/public/clinicas/{clinicaId}")
@RequiredArgsConstructor
public class AgendamentoPublicoController {

	private final AgendamentoPublicoService agendamentoPublicoService;
	private final PublicBookingRateLimiter rateLimiter;

	@Operation(summary = "Listar dentistas ativos da clínica, para o widget de agendamento público.")
	@GetMapping("/dentistas")
	public ResponseEntity<List<PublicDentistaDTO>> listarDentistas(@PathVariable final Long clinicaId) {
		return ResponseEntity.ok(this.agendamentoPublicoService.listarDentistas(clinicaId));
	}

	@Operation(summary = "Horários livres de um dentista em uma data, calculado no servidor.")
	@GetMapping("/agenda/disponibilidade")
	public ResponseEntity<List<String>> disponibilidade(@PathVariable final Long clinicaId,
			@RequestParam final Long dentistaId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate data) {
		return ResponseEntity.ok(this.agendamentoPublicoService.disponibilidade(clinicaId, dentistaId, data));
	}

	@Operation(summary = "Criar agendamento público (sem login). Sujeito a limite de tentativas por IP/CPF.")
	@PostMapping("/agendamentos")
	public ResponseEntity<PublicAgendamentoResponseDTO> agendar(@PathVariable final Long clinicaId,
			@Valid @RequestBody final PublicAgendamentoRequest request, final HttpServletRequest httpRequest) {
		this.rateLimiter.verificar(httpRequest.getRemoteAddr(), request.getCpf());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(this.agendamentoPublicoService.agendar(clinicaId, request));
	}
}
