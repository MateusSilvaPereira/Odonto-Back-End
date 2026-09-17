package com.msp.odonto_pro.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsultaDTO {

	private Long id;

	private Long pacienteId;

	private String nomePaciente;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate data;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	private LocalTime horario;

	private String procedimento;

	private String dentista;

	private String status;

	private LocalDateTime lembrete1DiaEnviadoEm;

	private LocalDateTime lembrete1HoraEnviadoEm;
}
