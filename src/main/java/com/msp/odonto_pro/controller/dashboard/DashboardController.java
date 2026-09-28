package com.msp.odonto_pro.controller.dashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.msp.odonto_pro.web.ApiPaths;

import com.msp.odonto_pro.dto.dashboard.DashboardResumoDTO;
import com.msp.odonto_pro.service.dashboard.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.V1 + "/dashboard")
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;

	@PreAuthorize("hasAnyAuthority('read:tratamento')")
	@Operation(summary = "Contagens globais para o dashboard.")
	@GetMapping("/resumo")
	public ResponseEntity<DashboardResumoDTO> resumo() {
		return ResponseEntity.ok(this.dashboardService.resumo());
	}
}
