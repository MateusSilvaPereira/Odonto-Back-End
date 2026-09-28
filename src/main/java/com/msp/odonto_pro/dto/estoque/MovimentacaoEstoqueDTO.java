package com.msp.odonto_pro.dto.estoque;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovimentacaoEstoqueDTO {

	private Long id;
	private Long itemEstoqueId;
	private String nomeItem;
	private String tipo;
	private Double quantidade;
	private String motivo;
	private LocalDateTime data;
	private String criadoPor;
}
