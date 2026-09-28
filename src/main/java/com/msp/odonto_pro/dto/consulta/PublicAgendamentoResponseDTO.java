package com.msp.odonto_pro.dto.consulta;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicAgendamentoResponseDTO {

	private Long consultaId;
	private LocalDate data;
	private LocalTime horario;
	private String procedimento;
	private String dentista;
	private String status;
}
