package com.msp.odonto_pro.dto.dentista;

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
public class DentistaDTO {

	private Long id;

	private String nome;

	private String cro;

	private String especialidade;

	private String telefone;

	private String email;

	private String corAgenda;

	private Boolean ativo;

	private String observacoes;

	private LocalDateTime criadoEm;

	/** Vazio/ausente = sem restrição de horário (dentista sempre disponível). */
	private List<HorarioAtendimentoDTO> horarioAtendimento;

	/** 0-100, opcional (default 0). */
	private Double percentualComissao;

	/** Somente leitura — gerenciada via POST/DELETE /dentistas/{id}/foto. */
	private String fotoUrl;
}
