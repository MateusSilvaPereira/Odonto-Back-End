package com.msp.odonto_pro.dto.consulta;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Body de {@code PATCH /consultas/{id}/reagendar}. */
@Data
public class ReagendarConsultaRequest {

	@NotNull(message = "data é obrigatório")
	private LocalDate data;

	@NotNull(message = "horario é obrigatório")
	private LocalTime horario;

	/** Opcional: omitido mantém o dentista atual da consulta. */
	private Long dentistaId;
}
