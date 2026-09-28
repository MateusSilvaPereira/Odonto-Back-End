package com.msp.odonto_pro.dto.orcamento;
import com.msp.odonto_pro.dto.parcela.ParcelaDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrcamentoDTO {

	private Long id;
	private Long pacienteId;
	private List<OrcamentoItemDTO> itens;
	private Double valorTotal;
	private String condicaoPagamento;
	private LocalDate validade;
	private String status;
	private LocalDateTime criadoEm;
	private String criadoPor;
	private LocalDateTime respondidoEm;

	/** Obrigatório na entrada (default 1 se omitido); eco do valor salvo na saída. */
	private Integer numeroParcelas;

	/** Opcional na entrada. Usado como vencimento da 1ª parcela quando o orçamento for aprovado. */
	private LocalDate primeiroVencimento;

	/** Null/omitido até o orçamento ser aprovado. */
	private List<ParcelaDTO> parcelas;

	/** Token do link público de aprovação/recusa (GET /public/orcamentos/{linkToken}). Somente leitura. */
	private String linkToken;
}
