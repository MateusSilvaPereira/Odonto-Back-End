package com.msp.odonto_pro.dto.evolucaoclinica;

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
public class EvolucaoClinicaDTO {

	private Long id;
	private Long pacienteId;
	private LocalDate data;
	private String dentista;
	private String queixaPrincipal;
	private String diagnostico;
	private String procedimentoRealizado;
	private String evolucao;
	private String prescricao;
	private LocalDateTime criadoEm;
	private String criadoPor;
	private Long retificaEvolucaoId;
	private Long consultaId;
}
