package com.msp.odonto_pro.dtos;

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
public class PagamentoDTO {

	private Long id;
	private Long itemTratamentoId;
	private Long pacienteId;
	private String nomePaciente;
	private String procedimento;
	private Double valor;
	private String formaPagamento;
	private LocalDate data;
	private String observacao;
	private LocalDateTime criadoEm;
}
