package com.msp.odonto_pro.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinanceiroResumoDTO {

	private Double totalOrcado;
	private Double totalRecebido;
	private Double totalPendente;
	private Double recebidoNoMes;
}
