package com.msp.odonto_pro.dto.parcela;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParcelaDTO {

	private Long id;

	private Long orcamentoId;

	private Integer numero;

	private Integer totalParcelas;

	private Double valor;

	private LocalDate vencimento;

	/** "pendente" | "paga" | "atrasada". */
	private String status;

	private LocalDateTime dataPagamento;
}
