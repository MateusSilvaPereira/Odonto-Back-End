package com.msp.odonto_pro.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

import com.msp.odonto_pro.entity.Dentista;
import com.msp.odonto_pro.entity.Paciente;

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

	private LocalDate data;

	private LocalTime hora;

	private String observacao;

	private String status;

	private Dentista dentista;

	private Paciente paciente;
}
