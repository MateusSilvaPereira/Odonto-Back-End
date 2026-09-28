package com.msp.odonto_pro.dto.convenioprocedimento;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConvenioProcedimentoDTO {

	private Long id;

	private Long convenioId;

	private String procedimento;

	private BigDecimal valor;
}
