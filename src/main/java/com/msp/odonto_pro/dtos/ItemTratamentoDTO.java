package com.msp.odonto_pro.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemTratamentoDTO {

	private Long id;
	private Long pacienteId;
	private String dente;
	private String procedimento;
	private Double valor;
	private String status;
	private LocalDateTime criadoEm;
	private String observacao;
	private Double valorPago;
}
