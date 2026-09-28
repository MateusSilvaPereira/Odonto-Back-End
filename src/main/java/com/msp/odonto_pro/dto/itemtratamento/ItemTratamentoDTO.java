package com.msp.odonto_pro.dto.itemtratamento;

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
	private Long dentistaId;
	/** Nome do profissional, resolvido via dentistaId (ou texto legado). Somente leitura. */
	private String profissional;
	private LocalDate dataPrevista;
	private LocalDate dataRealizada;
	private Long orcamentoId;

	/**
	 * Id da Consulta que originou este item (atendimento -> lançamento de
	 * procedimento realizado), quando aplicável. Opcional na entrada; null
	 * quando o item foi criado manualmente ou veio de aprovação de
	 * orçamento.
	 */
	private Long consultaId;
}
