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
public class DespesaDTO {

	private Long id;
	private String descricao;
	private String categoria;
	private Double valor;
	private LocalDate dataVencimento;
	private LocalDate dataPagamento;
	private String status;
	private String fornecedor;
	private String observacao;
	private LocalDateTime criadoEm;
}
