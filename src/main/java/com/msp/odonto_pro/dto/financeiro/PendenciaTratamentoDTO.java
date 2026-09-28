package com.msp.odonto_pro.dto.financeiro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PendenciaTratamentoDTO {

	private Long itemTratamentoId;
	private Long pacienteId;
	private String nomePaciente;
	private String procedimento;
	private Double valor;
	private Double valorPago;
	private Double saldo;
}
