package com.msp.odonto_pro.dto.listaespera;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListaEsperaDTO {

	private Long id;

	private Long pacienteId;

	/** Somente leitura, resolvido via join. */
	private String nomePaciente;

	private String procedimento;

	private Long dentistaId;

	/** Somente leitura, resolvido via join. */
	private String dentista;

	private String observacao;

	/** "aguardando" | "agendado" | "cancelado". */
	private String status;

	private LocalDateTime criadoEm;
}
