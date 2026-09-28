package com.msp.odonto_pro.controller.parcela;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.parcela.ParcelaDTO;
import com.msp.odonto_pro.service.parcela.ParcelaService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.V1 + "/parcelas")
@RequiredArgsConstructor
public class ParcelaController {

	private final ParcelaService parcelaService;

	@PreAuthorize("hasAuthority('write:financeiro')")
	@Operation(summary = "Marcar parcela como paga.")
	@PatchMapping("/{id}/pagar")
	public ResponseEntity<ParcelaDTO> pagar(@PathVariable final Long id) {
		return ResponseEntity.ok(this.parcelaService.pagar(id));
	}
}
