package com.msp.odonto_pro.dto.orcamento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Item de orçamento exposto publicamente — sem id (ver OrcamentoPublicoDTO). */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrcamentoPublicoItemDTO {

	private String dente;
	private String procedimento;
	private Double valor;
	private Double desconto;
}
