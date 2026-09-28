package com.msp.odonto_pro.service.dashboard;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.msp.odonto_pro.dto.dashboard.DashboardResumoDTO;
import com.msp.odonto_pro.repository.itemtratamento.ItemTratamentoRepository;
import com.msp.odonto_pro.repository.orcamento.OrcamentoRepository;
import com.msp.odonto_pro.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private final OrcamentoRepository orcamentoRepository;
	private final ItemTratamentoRepository itemTratamentoRepository;

	public DashboardResumoDTO resumo() {
		final Long clinicaId = SecurityUtils.clinicaIdAtual();
		final long orcamentosPendentes = this.orcamentoRepository
				.countByStatusAndValidadeGreaterThanEqualAndPaciente_ClinicaId("pendente", LocalDate.now(), clinicaId);
		final long tratamentosEmAndamento = this.itemTratamentoRepository
				.countByStatusAndPaciente_ClinicaId("em_andamento", clinicaId);

		return new DashboardResumoDTO(orcamentosPendentes, tratamentosEmAndamento);
	}
}
