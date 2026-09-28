package com.msp.odonto_pro.dto.orcamento;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO enxuto exposto pelo endpoint público de aprovação/recusa de
 * orçamento (sem autenticação). Não inclui id, pacienteId, orcamentoId nem
 * qualquer outro id interno — mesmo cuidado de ConsultaPublicaDTO.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrcamentoPublicoDTO {

	private String nomePaciente;

	/** Opcional — omitido se a clínica não tiver nome cadastrado. */
	private String nomeClinica;

	private List<OrcamentoPublicoItemDTO> itens;

	private Double valorTotal;

	private String condicaoPagamento;

	private Integer numeroParcelas;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate validade;

	/** "pendente" | "aprovado" | "recusado" | "expirado". */
	private String status;

	private boolean linkExpirado;
}
