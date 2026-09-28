package com.msp.odonto_pro.dto.orcamento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrcamentoItemDTO {

	private Long id;
	private String dente;
	private String procedimento;
	private Double valor;
	private Double desconto;
}
