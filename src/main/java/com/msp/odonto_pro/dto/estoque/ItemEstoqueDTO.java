package com.msp.odonto_pro.dto.estoque;

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
public class ItemEstoqueDTO {

	private Long id;
	private String nome;
	private String categoria;
	private String unidadeMedida;
	private Double quantidadeAtual;
	private Double quantidadeMinima;
	private String fornecedor;
	private LocalDate validade;
	private Double precoUnitario;
	private LocalDateTime atualizadoEm;
}
