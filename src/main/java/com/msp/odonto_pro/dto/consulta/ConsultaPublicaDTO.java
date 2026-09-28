package com.msp.odonto_pro.dto.consulta;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO enxuto exposto pelo endpoint público de confirmação de consulta
 * (sem autenticação). Não inclui id, pacienteId nem qualquer dado sensível
 * do paciente (telefone/cpf/email).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsultaPublicaDTO {

	private String nomePaciente;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate data;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	private LocalTime horario;

	private String procedimento;

	private String dentista;

	private String status;

	private boolean linkExpirado;
}
