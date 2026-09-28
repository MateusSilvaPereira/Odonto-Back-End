package com.msp.odonto_pro.dto.financeiro;

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
	/** Somente leitura, resolvido via itemTratamentoId -> ItemTratamento.dentistaId. */
	private Long dentistaId;
	private String dentista;
	private Double valor;
	private String formaPagamento;
	private LocalDate data;
	private String observacao;
	private LocalDateTime criadoEm;
}
