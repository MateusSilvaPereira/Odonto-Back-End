package com.msp.odonto_pro.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResumoDTO {

	private long orcamentosPendentes;
	private long tratamentosEmAndamento;
}
